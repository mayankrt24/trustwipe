import React, { useState, useEffect } from 'react';
import { 
  History, 
  Search, 
  ChevronRight,
  ShieldCheck,
  Calendar,
  Clock,
  Hash,
  Loader2,
  Filter,
  ArrowUpDown
} from 'lucide-react';
import { reportApi, assetApi } from '../api/api';

const WipeHistoryPage = () => {
  const [reports, setReports] = useState([]);
  const [assets, setAssets] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [reportsRes, assetsRes] = await Promise.all([
          reportApi.getAll(),
          assetApi.getAll()
        ]);
        
        const assetsMap = assetsRes.data.reduce((acc, asset) => {
          acc[asset.id] = asset;
          return acc;
        }, {});
        
        setReports(reportsRes.data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp)));
        setAssets(assetsMap);
      } catch (error) {
        console.error('Error fetching history', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const formatDuration = (ms) => {
    return `${(ms / 1000).toFixed(2)}s`;
  };

  const filteredReports = reports.filter(report => {
    const assetName = assets[report.assetId]?.name || 'Unknown Device';
    const matchesSearch = assetName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                         report.verificationHash?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesFilter = filterType === 'ALL' || report.wipeType === filterType;
    return matchesSearch && matchesFilter;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Wipe History</h2>
          <p className="text-slate-500 dark:text-slate-400">Detailed logs of all destruction operations</p>
        </div>
        <div className="flex gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search history..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg py-2 pl-10 pr-4 text-sm outline-none focus:ring-2 focus:ring-primary-500 transition-all"
            />
          </div>
          <div className="relative inline-block">
            <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <select 
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg py-2 pl-10 pr-4 text-sm outline-none focus:ring-2 focus:ring-primary-500 transition-all appearance-none cursor-pointer"
            >
              <option value="ALL">All Types</option>
              <option value="FULL">Full Wipe</option>
              <option value="PARTIAL">Partial Wipe</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-800">
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Device Name</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Wipe Type</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Date & Time</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Duration</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">Verification ID</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {isLoading ? (
              <tr>
                <td colSpan="6" className="px-6 py-12 text-center">
                  <Loader2 className="w-8 h-8 animate-spin mx-auto text-primary-600" />
                </td>
              </tr>
            ) : filteredReports.length === 0 ? (
              <tr>
                <td colSpan="6" className="px-6 py-12 text-center opacity-60">
                  <History className="w-12 h-12 mx-auto mb-4 text-slate-400" />
                  <p className="text-slate-500 text-sm">No operations found in history</p>
                </td>
              </tr>
            ) : filteredReports.map((report) => (
              <tr key={report.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors group">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded bg-primary-50 dark:bg-primary-900/20 flex items-center justify-center">
                      <ShieldCheck className="w-4 h-4 text-primary-600 dark:text-primary-400" />
                    </div>
                    <span className="font-medium text-slate-900 dark:text-white">
                      {assets[report.assetId]?.name || 'Unknown Device'}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    report.wipeType === 'FULL' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                  }`}>
                    {report.wipeType}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col">
                    <span className="text-sm text-slate-900 dark:text-white">{new Date(report.timestamp).toLocaleDateString()}</span>
                    <span className="text-[10px] text-slate-500 dark:text-slate-400">{new Date(report.timestamp).toLocaleTimeString()}</span>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                  {formatDuration(report.duration)}
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                    report.finalStatus === 'SUCCESS' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                  }`}>
                    {report.finalStatus}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <span className="text-xs font-mono text-slate-500 dark:text-slate-400">{report.verificationHash?.substring(0, 12)}...</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default WipeHistoryPage;
