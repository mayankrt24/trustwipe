import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Download, 
  Search, 
  ChevronRight,
  ShieldCheck,
  Calendar,
  Clock,
  Hash,
  Loader2
} from 'lucide-react';
import { reportApi, assetApi } from '../api/api';
import { jsPDF } from 'jspdf';
import 'jspdf-autotable';

const ReportsPage = () => {
  const [reports, setReports] = useState([]);
  const [assets, setAssets] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

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
        
        setReports(reportsRes.data);
        setAssets(assetsMap);
      } catch (error) {
        console.error('Error fetching reports', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const filteredReports = reports.filter(report => {
    const assetName = assets[report.assetId]?.name || 'Unknown Device';
    return assetName.toLowerCase().includes(searchTerm.toLowerCase()) || 
           report.verificationHash?.toLowerCase().includes(searchTerm.toLowerCase());
  });

  const downloadPDF = (report) => {
    const asset = assets[report.assetId] || { name: 'Unknown Device' };
    const doc = new jsPDF();

    // Color Palette
    const primaryColor = [14, 165, 233]; // primary-600
    const darkColor = [15, 23, 42]; // slate-900
    const grayColor = [100, 116, 139]; // slate-500

    // Header
    doc.setFillColor(...primaryColor);
    doc.rect(0, 0, 210, 45, 'F');
    
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(26);
    doc.setFont('helvetica', 'bold');
    doc.text('CERTIFICATE OF DESTRUCTION', 20, 28);
    
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text('TrustWipe Secure Data Erasure Service • Official Verification Report', 20, 36);

    // Main Details Section
    doc.setTextColor(...darkColor);
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('Operational Summary', 20, 65);
    
    doc.autoTable({
      startY: 72,
      head: [['Field', 'Description']],
      body: [
        ['Device Name', asset.name],
        ['Wipe Type', report.wipeType === 'FULL' ? 'FULL SYSTEM WIPE' : 'PARTIAL (CUSTOM SELECTION)'],
        ['Status', report.finalStatus],
        ['Date/Time', new Date(report.timestamp).toLocaleString()],
        ['Wipe Strategy', `NIST 800-88 rev1 (${report.passes} Passes)`],
        ['Execution Time', `${(report.duration / 1000).toFixed(2)} seconds`],
        ['Verification ID', report.verificationHash],
      ],
      theme: 'grid',
      headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' },
      styles: { cellPadding: 5, fontSize: 10 },
      columnStyles: { 0: { fontStyle: 'bold', width: 50 } }
    });

    // Wipe Scope Section
    let currentY = doc.lastAutoTable.finalY + 20;
    
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('Destruction Scope', 20, currentY);
    currentY += 8;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    if (report.wipeType === 'FULL') {
      doc.setTextColor(...grayColor);
      doc.text('• Entire device storage and all logical partitions have been sanitized.', 25, currentY);
      currentY += 15;
    } else {
      doc.setTextColor(...darkColor);
      doc.text('Specific files and directories targeted for destruction:', 25, currentY);
      currentY += 10;
      
      // If we have paths stored in the report (need to ensure they are available)
      // For now, listing wiped files as the scope if it was partial
    }

    // Wiped Files Section
    if (report.wipedFiles && report.wipedFiles.length > 0) {
      if (currentY > 250) { doc.addPage(); currentY = 20; }
      
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(...darkColor);
      doc.text('List of Sanitized Items', 20, currentY);
      currentY += 10;

      const fileData = report.wipedFiles.map((path, index) => [`${index + 1}.`, path]);
      
      doc.autoTable({
        startY: currentY,
        head: [['#', 'File System Path']],
        body: fileData,
        theme: 'striped',
        styles: { fontSize: 8, cellPadding: 2 },
        headStyles: { fillColor: [241, 245, 249], textColor: darkColor, fontStyle: 'bold' },
        columnStyles: { 0: { width: 10 } }
      });
    } else if (report.wipeType === 'PARTIAL') {
      doc.setTextColor(...grayColor);
      doc.setFont('helvetica', 'italic');
      doc.text('No individual file paths were recorded for this operation.', 25, currentY);
    }

    // Footer
    const pageCount = doc.internal.getNumberOfPages();
    for(let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setDrawColor(226, 232, 240);
      doc.line(20, 280, 190, 280);
      
      doc.setFontSize(8);
      doc.setTextColor(...grayColor);
      doc.setFont('helvetica', 'normal');
      doc.text(`Certificate Hash: ${report.verificationHash}`, 20, 287);
      doc.text(`Page ${i} of ${pageCount}`, 170, 287);
      doc.text(`This document serves as legal evidence of secure data destruction performed by TrustWipe.`, 20, 292);
    }

    doc.save(`Certificate_${asset.name.replace(/[^a-z0-9]/gi, '_')}_${new Date().getTime()}.pdf`);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Compliance Reports</h2>
          <p className="text-slate-500 dark:text-slate-400">View and download verification certificates</p>
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input 
            type="text" 
            placeholder="Search reports..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg py-2 pl-10 pr-4 text-sm outline-none focus:ring-2 focus:ring-primary-500 transition-all"
          />
        </div>
      </div>

      <div className="grid gap-4">
        {isLoading ? (
          <div className="card p-12 text-center">
            <Loader2 className="w-8 h-8 animate-spin mx-auto text-primary-600" />
          </div>
        ) : filteredReports.length === 0 ? (
          <div className="card p-12 text-center opacity-60">
            <FileText className="w-12 h-12 mx-auto mb-4 text-slate-400" />
            <p className="text-slate-500">{searchTerm ? 'No matching reports found' : 'No reports generated yet'}</p>
          </div>
        ) : filteredReports.map((report) => (
          <div key={report.id} className="card p-4 hover:shadow-md transition-all group">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-primary-50 dark:bg-primary-900/20 flex items-center justify-center">
                  <ShieldCheck className="w-6 h-6 text-primary-600 dark:text-primary-400" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-900 dark:text-white">
                      {assets[report.assetId]?.name || 'Unknown Device'}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      report.wipeType === 'FULL' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                    }`}>
                      {report.wipeType} WIPE
                    </span>
                  </div>
                  <div className="flex items-center gap-4 mt-1 text-xs text-slate-500 dark:text-slate-400">
                    <span className="flex items-center gap-1"><Calendar className="w-3 h-3" /> {new Date(report.timestamp).toLocaleDateString()}</span>
                    <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {new Date(report.timestamp).toLocaleTimeString()}</span>
                    <span className="flex items-center gap-1"><Hash className="w-3 h-3" /> {report.verificationHash?.substring(0, 8) || 'N/A'}...</span>
                  </div>
                </div>
              </div>
              
              <div className="flex items-center gap-3">
                <button 
                  onClick={() => downloadPDF(report)}
                  className="btn btn-outline gap-2 text-xs py-2 px-4 border-slate-200 dark:border-slate-800"
                >
                  <Download className="w-4 h-4" />
                  Certificate
                </button>
                <button className="p-2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors">
                  <ChevronRight className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ReportsPage;
