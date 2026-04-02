import React, { useState, useEffect } from 'react';
import { 
  HardDrive, 
  Activity, 
  CheckCircle2, 
  AlertCircle,
  RefreshCw,
  Plus
} from 'lucide-react';
import { motion } from 'framer-motion';
import { assetApi, reportApi, wipeApi } from '../api/api';
import { useToast } from '../context/ToastContext';

const StatCard = ({ title, value, icon: Icon, color }) => (
  <div className="card p-6 flex items-center justify-between group hover:shadow-lg transition-all duration-300 cursor-default">
    <div>
      <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{title}</p>
      <p className="text-2xl font-bold mt-1 text-slate-900 dark:text-white group-hover:scale-110 origin-left transition-transform">{value}</p>
    </div>
    <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${color} group-hover:rotate-12 transition-transform shadow-lg`}>
      <Icon className="w-6 h-6 text-white" />
    </div>
  </div>
);

const ActiveWipeItem = ({ asset, progress }) => (
  <div className="card p-4 space-y-3 hover:shadow-md transition-shadow">
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-lg bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center group">
          <HardDrive className="w-5 h-5 text-primary-600 dark:text-primary-400 group-hover:animate-pulse" />
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-900 dark:text-white">{asset.name}</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 animate-pulse">{progress.status}</p>
        </div>
      </div>
      <span className="text-sm font-bold text-primary-600 dark:text-primary-400">{progress.percentage}%</span>
    </div>
    <div className="w-full h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
      <motion.div 
        initial={{ width: 0 }}
        animate={{ width: `${progress.percentage}%` }}
        className="h-full bg-primary-600 dark:bg-primary-500 shadow-[0_0_10px_rgba(14,165,233,0.5)]"
        transition={{ type: "spring", stiffness: 50, damping: 20 }}
      />
    </div>
  </div>
);

const DashboardPage = () => {
  const [stats, setStats] = useState({ total: 0, active: 0, completed: 0 });
  const [activeWipes, setActiveWipes] = useState([]);
  const [recentReports, setRecentReports] = useState([]); 
  const [isLoading, setIsLoading] = useState(true);
  const [assets, setAssets] = useState([]);
  const { addToast } = useToast();

  const fetchData = async () => {
    try {
      const [assetsRes, reportsRes] = await Promise.all([
        assetApi.getAll(),
        reportApi.getAll()
      ]);

      const fetchedAssets = assetsRes.data;
      const reports = reportsRes.data;

      // Monitor status changes for toasts
      fetchedAssets.forEach(newAsset => {
        const oldAsset = assets.find(a => a.id === newAsset.id);
        if (oldAsset && oldAsset.status === 'WIPING' && newAsset.status === 'WIPED') {
          addToast(`Wipe completed for ${newAsset.name}`, 'success');
        } else if (oldAsset && oldAsset.status === 'WIPING' && newAsset.status === 'FAILED') {
          addToast(`Wipe failed for ${newAsset.name}`, 'error');
        }
      });

      setAssets(fetchedAssets);

      // Show WIPING, and recently WIPED/FAILED in the "Live" section
      const activeAssets = fetchedAssets.filter(a => 
        a.status === 'WIPING' || 
        a.status === 'IN_PROGRESS' ||
        a.status === 'WIPED' ||
        a.status === 'FAILED'
      ).sort((a, b) => {
        if (a.status === 'WIPING' && b.status !== 'WIPING') return -1;
        if (a.status !== 'WIPING' && b.status === 'WIPING') return 1;
        return 0;
      }).slice(0, 5);
      
      setStats({
        total: fetchedAssets.length,
        active: fetchedAssets.filter(a => a.status === 'WIPING').length,
        completed: reports.length
      });

      const recent = reports
        .filter(r => r.finalStatus === 'SUCCESS')
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
        .slice(0, 5);
      
      const assetsMap = fetchedAssets.reduce((acc, a) => ({ ...acc, [a.id]: a.name }), {});
      setRecentReports(recent.map(r => ({ ...r, assetName: assetsMap[r.assetId] || 'Unknown Device' })));

      const progressPromises = activeAssets.map(async (asset) => {
        const progRes = await wipeApi.getProgress(asset.id);
        return { asset, progress: progRes.data };
      });

      const wipes = await Promise.all(progressPromises);
      setActiveWipes(wipes);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000); 
    return () => clearInterval(interval);
  }, [assets]);

  if (isLoading) return <div className="flex items-center justify-center h-full"><RefreshCw className="w-8 h-8 animate-spin text-primary-600" /></div>;

  return (
    <div className="space-y-8">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Dashboard Overview</h2>
        <p className="text-slate-500 dark:text-slate-400">Real-time status of data destruction operations</p>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard title="Total Devices" value={stats.total} icon={HardDrive} color="bg-blue-600" />
        <StatCard title="Active Wipes" value={stats.active} icon={Activity} color="bg-amber-500" />
        <StatCard title="Completed Wipes" value={stats.completed} icon={CheckCircle2} color="bg-emerald-600" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="space-y-4">
          <h3 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Activity className="w-5 h-5 text-primary-600" />
            Recent Activity
          </h3>
          <div className="space-y-4">
            {activeWipes.length > 0 ? (
              activeWipes.map(({ asset, progress }) => (
                <ActiveWipeItem key={asset.id} asset={asset} progress={progress} />
              ))
            ) : (
              <div className="card p-12 flex flex-col items-center justify-center text-center opacity-60">
                <AlertCircle className="w-12 h-12 mb-4 text-slate-400" />
                <p className="text-slate-500">No recent operations found</p>
              </div>
            )}
          </div>
        </div>

        <div className="space-y-4">
          <h3 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-primary-600" />
            Success History
          </h3>
          <div className="card overflow-hidden">
            <div className="p-4 bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-800 flex justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Asset</span>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500 text-right">Status</span>
            </div>
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {recentReports.length > 0 ? (
                recentReports.map((report) => (
                  <motion.div 
                    layout
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    key={report.id} 
                    className="p-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                      </div>
                      <span className="text-sm text-slate-900 dark:text-white">{report.assetName}</span>
                    </div>
                    <span className="px-2 py-1 bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 text-[10px] font-bold rounded shadow-sm">SUCCESS</span>
                  </motion.div>
                ))
              ) : (
                <div className="p-12 text-center opacity-60">
                  <p className="text-slate-500 text-sm">No recent wipes completed</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
