package com.wozlla.idea;

public interface IExternalFileCompiler {

    void addCompilerListener(ICompilerListener listener);
    void removeCompilerListener(ICompilerListener listener);

    public static interface ICompilerListener {

        void onError(String errorMsg);

        void onSuccess();

        void compile();

        void initCompiler();

    }
}
