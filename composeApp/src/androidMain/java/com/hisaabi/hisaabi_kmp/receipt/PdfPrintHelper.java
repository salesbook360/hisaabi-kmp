package com.hisaabi.hisaabi_kmp.receipt;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class PdfPrintHelper {
    
    public interface PdfGenerationCallback {
        void onSuccess(File file);
        void onError(Exception exception);
    }
    
    public static void generatePdfFromWebView(
            WebView webView,
            File outputFile,
            PdfGenerationCallback callback
    ) {
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 1200, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
        
        PrintDocumentAdapter documentAdapter = webView.createPrintDocumentAdapter(outputFile.getName());
        
        documentAdapter.onLayout(
                null,
                printAttributes,
                null,
                new PrintDocumentAdapter.LayoutResultCallback() {
                    @Override
                    public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                        documentAdapter.onWrite(
                                new android.print.PageRange[]{android.print.PageRange.ALL_PAGES},
                                getOutputFileDescriptor(outputFile),
                                null,
                                new PrintDocumentAdapter.WriteResultCallback() {
                                    @Override
                                    public void onWriteFinished(android.print.PageRange[] pages) {
                                        super.onWriteFinished(pages);
                                        webView.destroy();
                                        callback.onSuccess(outputFile);
                                    }
                                    
                                    @Override
                                    public void onWriteFailed(CharSequence error) {
                                        super.onWriteFailed(error);
                                        webView.destroy();
                                        callback.onError(new Exception(error.toString()));
                                    }
                                    
                                    @Override
                                    public void onWriteCancelled() {
                                        super.onWriteCancelled();
                                        webView.destroy();
                                        callback.onError(new Exception("PDF Write cancelled"));
                                    }
                                }
                        );
                    }
                    
                    @Override
                    public void onLayoutFailed(CharSequence error) {
                        super.onLayoutFailed(error);
                        webView.destroy();
                        callback.onError(new Exception(error.toString()));
                    }
                },
                null
        );
    }
    
    private static ParcelFileDescriptor getOutputFileDescriptor(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

