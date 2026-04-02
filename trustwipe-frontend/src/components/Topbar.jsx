import React from 'react';
import { Sun, Moon, User } from 'lucide-react';

const Topbar = ({ toggleDarkMode, isDarkMode }) => {
  const userEmail = localStorage.getItem('userEmail') || 'Guest';
  const userName = userEmail !== 'Guest' 
    ? userEmail.split('@')[0].charAt(0).toUpperCase() + userEmail.split('@')[0].slice(1) 
    : 'Guest User';

  return (
    <header className="h-16 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between px-6 sticky top-0 z-10 transition-colors">
      <div className="flex items-center gap-2">
        <h1 className="text-sm font-medium text-slate-500 dark:text-slate-400">Project / Dashboard</h1>
      </div>

      <div className="flex items-center gap-4">
        <button
          onClick={toggleDarkMode}
          className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label="Toggle Theme"
        >
          {isDarkMode ? (
            <Sun className="w-5 h-5 text-slate-600 dark:text-slate-400" />
          ) : (
            <Moon className="w-5 h-5 text-slate-600 dark:text-slate-400" />
          )}
        </button>

        <div className="h-8 w-[1px] bg-slate-200 dark:border-slate-800" />

        <div className="flex items-center gap-3">
          <div className="text-right">
            <p className="text-sm font-medium text-slate-900 dark:text-white leading-none">{userName}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400">{userEmail}</p>
          </div>
          <div className="w-9 h-9 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
            <User className="w-5 h-5 text-primary-600 dark:text-primary-400" />
          </div>
        </div>
      </div>
    </header>
  );
};

export default Topbar;
