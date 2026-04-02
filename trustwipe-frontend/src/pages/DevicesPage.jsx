import React, { useState, useEffect } from 'react';
import { 
  HardDrive, 
  Trash2, 
  Search, 
  Filter,
  MoreVertical,
  ShieldAlert,
  FolderOpen,
  File,
  ChevronRight,
  CheckSquare,
  Square,
  X,
  Loader2,
  RefreshCw,
  Activity
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { assetApi, wipeApi } from '../api/api';
import { useToast } from '../context/ToastContext';

const FileExplorer = ({ rootPath, onStartWipe, onClose }) => {
  const [currentPath, setCurrentPath] = useState(rootPath);
  const [files, setFiles] = useState([]);
  const [selectedPaths, setSelectedPaths] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchFiles = async () => {
      setIsLoading(true);
      try {
        const res = await assetApi.listPath(currentPath);
        const sorted = res.data.sort((a, b) => {
          if (a.isDirectory === b.isDirectory) return a.name.localeCompare(b.name);
          return a.isDirectory ? -1 : 1;
        });
        setFiles(sorted);
      } catch (error) {
        console.error('Error listing path', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchFiles();
  }, [currentPath]);

  const isChildOfSelected = (path) => {
    return selectedPaths.some(p => path.startsWith(p + '\\') || path.startsWith(p + '/'));
  };

  const isSelected = (path) => {
    return selectedPaths.includes(path) || isChildOfSelected(path);
  };

  const togglePath = (path, isDirectory) => {
    setSelectedPaths(prev => {
      if (prev.includes(path)) {
        return prev.filter(p => p !== path);
      } else {
        // If parent is already selected, no need to add child
        if (isChildOfSelected(path)) return prev;
        
        // If selecting a parent, remove any children that were individually selected
        const newSelection = prev.filter(p => !p.startsWith(path + '\\') && !p.startsWith(path + '/'));
        return [...newSelection, path];
      }
    });
  };

  const navigateUp = () => {
    const parts = currentPath.split(/[\\\/]/);
    if (parts.length > 1) {
      if (parts[parts.length - 1] === '') parts.pop();
      parts.pop();
      const newPath = parts.join('\\') + '\\';
      if (newPath.length >= rootPath.length) {
        setCurrentPath(newPath);
      }
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between text-xs text-slate-500 bg-slate-100 dark:bg-slate-800 p-2 rounded">
        <div className="flex items-center gap-2 truncate flex-1">
          <FolderOpen className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{currentPath}</span>
        </div>
        <button 
          onClick={navigateUp}
          disabled={currentPath === rootPath}
          className="ml-2 px-2 py-1 hover:bg-slate-200 dark:hover:bg-slate-700 rounded transition-colors disabled:opacity-30 flex items-center gap-1"
        >
          <ChevronRight className="w-3 h-3 rotate-180" />
          Back
        </button>
      </div>

      <div className="max-h-[300px] min-h-[200px] overflow-y-auto border border-slate-200 dark:border-slate-800 rounded-lg bg-slate-50 dark:bg-slate-900/50 p-1">
        {isLoading ? (
          <div className="p-12 text-center"><Loader2 className="w-6 h-6 animate-spin mx-auto text-primary-600" /></div>
        ) : files.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm italic flex flex-col items-center gap-2">
            <File className="w-8 h-8 opacity-20" />
            No files or folders found
          </div>
        ) : (
          files.map((file) => {
            const selected = isSelected(file.path);
            const parentSelected = isChildOfSelected(file.path);
            
            return (
              <div 
                key={file.path}
                className={`flex items-center justify-between p-2 rounded-md transition-all group mb-0.5 ${
                  selected ? 'bg-primary-50 dark:bg-primary-900/20' : 'hover:bg-white dark:hover:bg-slate-800'
                }`}
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <button 
                    onClick={() => togglePath(file.path, file.isDirectory)}
                    disabled={parentSelected}
                    className={`flex-shrink-0 transition-colors ${parentSelected ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
                  >
                    {selected ? (
                      <CheckSquare className={`w-5 h-5 ${parentSelected ? 'text-primary-400' : 'text-primary-600'}`} />
                    ) : (
                      <Square className="w-5 h-5 text-slate-400 group-hover:text-slate-500" />
                    )}
                  </button>
                  
                  <div 
                    className="flex items-center gap-3 flex-1 cursor-pointer truncate py-1"
                    onClick={() => file.isDirectory ? setCurrentPath(file.path) : togglePath(file.path, false)}
                  >
                    {file.isDirectory ? (
                      <FolderOpen className="w-4 h-4 text-amber-500 flex-shrink-0" />
                    ) : (
                      <File className="w-4 h-4 text-blue-500 flex-shrink-0" />
                    )}
                    <span className={`text-sm truncate ${selected ? 'font-medium text-primary-900 dark:text-primary-100' : 'text-slate-700 dark:text-slate-300'}`}>
                      {file.name}
                    </span>
                  </div>
                </div>
                
                {file.isDirectory && (
                  <button 
                    onClick={(e) => { e.stopPropagation(); setCurrentPath(file.path); }} 
                    className="p-1.5 text-slate-400 hover:text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/30 rounded-md transition-all"
                    title="Open folder"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                )}
              </div>
            );
          })
        )}
      </div>
      
      <div className="flex items-center justify-between gap-4 pt-2">
        <div className="flex flex-col">
          <p className="text-xs font-bold text-slate-900 dark:text-white">
            {selectedPaths.length} items selected
          </p>
          <p className="text-[10px] text-slate-500">Folders will be wiped recursively</p>
        </div>
        <div className="flex gap-2">
          <button onClick={onClose} className="btn btn-outline text-xs px-4">Cancel</button>
          <button 
            disabled={selectedPaths.length === 0}
            onClick={() => onStartWipe(selectedPaths)}
            className="btn btn-primary text-xs px-6 gap-2 shadow-lg shadow-primary-600/20"
          >
            <Trash2 className="w-4 h-4" />
            Wipe Selected
          </button>
        </div>
      </div>
    </div>
  );
};

const WipeModal = ({ asset, onClose, onRefresh }) => {
  const [mode, setMode] = useState('choice'); // choice, partial, confirming
  const { addToast } = useToast();

  const handleFullWipe = async () => {
    try {
      addToast(`Full wipe started for ${asset.name}`, 'info');
      await wipeApi.fullWipe(asset.id);
      onRefresh();
      onClose();
    } catch (error) {
      addToast(`Failed to start wipe for ${asset.name}`, 'error');
      console.error('Wipe failed', error);
    }
  };

  const handlePartialWipe = async (paths) => {
    try {
      addToast(`Partial wipe started for ${asset.name}`, 'info');
      await wipeApi.partialWipe(asset.id, paths);
      onRefresh();
      onClose();
    } catch (error) {
      addToast(`Failed to start partial wipe for ${asset.name}`, 'error');
      console.error('Partial wipe failed', error);
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4"
    >
      <motion.div 
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="card max-w-lg w-full p-6 shadow-2xl relative"
      >
        <button onClick={onClose} className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 transition-colors">
          <X className="w-6 h-6" />
        </button>

        <div className="mb-6">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <ShieldAlert className="w-6 h-6 text-red-500" />
            Secure Wipe: {asset.name}
          </h3>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Data recovery will be impossible after this operation.
          </p>
        </div>

        {mode === 'choice' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button 
              onClick={() => setMode('confirming')}
              className="flex flex-col items-center gap-4 p-6 rounded-xl border-2 border-slate-100 dark:border-slate-800 hover:border-red-500 dark:hover:border-red-500 transition-all text-center group"
            >
              <Trash2 className="w-10 h-10 text-slate-400 group-hover:text-red-500" />
              <div>
                <p className="font-bold text-slate-900 dark:text-white">Full Wipe</p>
                <p className="text-xs text-slate-500 mt-1">Entire device including system files.</p>
              </div>
            </button>
            <button 
              onClick={() => setMode('partial')}
              className="flex flex-col items-center gap-4 p-6 rounded-xl border-2 border-slate-100 dark:border-slate-800 hover:border-primary-500 dark:hover:border-primary-500 transition-all text-center group"
            >
              <FolderOpen className="w-10 h-10 text-slate-400 group-hover:text-primary-500" />
              <div>
                <p className="font-bold text-slate-900 dark:text-white">Partial Wipe</p>
                <p className="text-xs text-slate-500 mt-1">Select specific files and folders.</p>
              </div>
            </button>
          </div>
        )}

        {mode === 'partial' && (
          <FileExplorer 
            rootPath={asset.name}
            onClose={() => setMode('choice')} 
            onStartWipe={handlePartialWipe} 
          />
        )}

        {mode === 'confirming' && (
          <div className="space-y-6">
            <div className="p-4 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-100 dark:border-red-900/30">
              <p className="text-sm text-red-700 dark:text-red-400 leading-relaxed">
                <strong>WARNING:</strong> This will perform a NIST 800-88 3-pass wipe on the entire drive <strong>{asset.name}</strong>. All partitions and data will be permanently destroyed.
              </p>
            </div>
            <div className="flex gap-3 justify-end">
              <button onClick={() => setMode('choice')} className="btn btn-outline">Go Back</button>
              <button onClick={handleFullWipe} className="btn bg-red-600 hover:bg-red-700 text-white shadow-lg shadow-red-600/20">Confirm Full Wipe</button>
            </div>
          </div>
        )}
      </motion.div>
    </motion.div>
  );
};

const DevicesPage = () => {
  const [assets, setAssets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isScanning, setIsScanning] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState(null);
  const [activeWipes, setActiveWipes] = useState({});
  const { addToast } = useToast();
  
  // Use a ref to store previous assets for toast comparison without triggering re-renders
  const prevAssetsRef = React.useRef([]);

  const fetchAssets = async () => {
    try {
      const res = await assetApi.getAll();
      const fetchedAssets = res.data;
      
      // Monitor status changes for toasts using the ref
      fetchedAssets.forEach(newAsset => {
        const oldAsset = prevAssetsRef.current.find(a => a.id === newAsset.id);
        if (oldAsset && oldAsset.status === 'WIPING' && newAsset.status === 'WIPED') {
          addToast(`Wipe completed for ${newAsset.name}`, 'success');
        } else if (oldAsset && oldAsset.status === 'WIPING' && newAsset.status === 'FAILED') {
          addToast(`Wipe failed for ${newAsset.name}`, 'error');
        }
      });

      // Update ref and state
      prevAssetsRef.current = fetchedAssets;
      setAssets(fetchedAssets);

      // Check for active wipes and fetch progress
      const active = fetchedAssets.filter(a => 
        a.status === 'WIPING' || a.status === 'IN_PROGRESS' || a.status.startsWith('IN_PROGRESS')
      );

      if (active.length > 0) {
        const progressMap = {};
        await Promise.all(active.map(async (asset) => {
          const progRes = await wipeApi.getProgress(asset.id);
          progressMap[asset.id] = progRes.data;
        }));
        setActiveWipes(progressMap);
      } else {
        setActiveWipes({});
      }
    } catch (error) {
      console.error('Error fetching assets', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleScan = async () => {
    setIsScanning(true);
    addToast('Scan started...', 'info');
    try {
      const res = await assetApi.scan();
      await fetchAssets();
      addToast(`${res.data.length} devices detected`, 'success');
    } catch (error) {
      addToast('Scan failed', 'error');
      console.error('Scan failed', error);
    } finally {
      setIsScanning(false);
    }
  };

  useEffect(() => {
    fetchAssets();
    const interval = setInterval(fetchAssets, 3000); 
    return () => clearInterval(interval);
  }, []); // Dependency array is now empty for clean mount/unmount polling logic

  const formatSize = (bytes) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Connected Devices</h2>
          <p className="text-slate-500 dark:text-slate-400">Manage and secure your hardware assets</p>
        </div>
        <div className="flex gap-2">
          <button 
            onClick={handleScan}
            disabled={isScanning}
            className="btn btn-primary gap-2 px-4 disabled:opacity-70 group relative overflow-hidden"
          >
            <RefreshCw className={`w-4 h-4 ${isScanning ? 'animate-spin' : 'group-hover:rotate-180 transition-transform duration-500'}`} />
            {isScanning ? 'Scanning...' : 'Scan Devices'}
            <div className="absolute inset-0 bg-white/10 translate-y-full group-hover:translate-y-0 transition-transform duration-300" />
          </button>
        </div>
      </div>

      <div className="card overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-800">
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Device Name</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Type</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Capacity</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Status & Progress</th>
              <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {isLoading ? (
              <tr>
                <td colSpan="5" className="px-6 py-12 text-center">
                  <Loader2 className="w-8 h-8 animate-spin mx-auto text-primary-600" />
                </td>
              </tr>
            ) : assets.map((asset) => {
              const progress = activeWipes[asset.id];
              const isWiping = asset.status === 'WIPING' || asset.status === 'IN_PROGRESS' || asset.status.startsWith('IN_PROGRESS');
              
              return (
                <tr key={asset.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors group">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded bg-slate-100 dark:bg-slate-800 flex items-center justify-center group-hover:scale-110 transition-transform">
                        <HardDrive className="w-4 h-4 text-slate-600 dark:text-slate-400" />
                      </div>
                      <span className="font-medium text-slate-900 dark:text-white">{asset.name}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-xs text-slate-600 dark:text-slate-400">{asset.type}</span>
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                    {formatSize(asset.size)}
                  </td>
                  <td className="px-6 py-4 min-w-[200px]">
                    <div className="space-y-1.5">
                      <div className="flex justify-between items-center">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold transition-colors ${
                          asset.status === 'WIPED' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' :
                          asset.status === 'FAILED' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400' :
                          isWiping ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400' :
                          'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                        }`}>
                          {isWiping && progress ? progress.status : asset.status}
                        </span>
                        {isWiping && progress && (
                          <span className="text-[10px] font-bold text-amber-600 dark:text-amber-400">{progress.percentage}%</span>
                        )}
                        {asset.status === 'WIPED' && (
                          <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400">100%</span>
                        )}
                      </div>
                      <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                        <motion.div 
                          initial={false}
                          animate={{ 
                            width: isWiping && progress ? `${progress.percentage}%` : 
                                   asset.status === 'WIPED' ? '100%' : '0%',
                            backgroundColor: asset.status === 'WIPED' ? '#10b981' : '#f59e0b'
                          }}
                          transition={{ type: "spring", stiffness: 50, damping: 20 }}
                          className="h-full"
                        />
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button 
                      onClick={() => setSelectedAsset(asset)}
                      disabled={isWiping}
                      className="btn btn-outline py-1.5 px-3 text-xs gap-2 border-red-200 dark:border-red-900/30 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 disabled:opacity-30 transition-all hover:scale-105 active:scale-95"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      {isWiping ? 'In Progress...' : 'Wipe'}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <AnimatePresence>
        {selectedAsset && (
          <WipeModal 
            asset={selectedAsset} 
            onClose={() => setSelectedAsset(null)} 
            onRefresh={fetchAssets}
          />
        )}
      </AnimatePresence>
    </div>
  );
};

export default DevicesPage;
