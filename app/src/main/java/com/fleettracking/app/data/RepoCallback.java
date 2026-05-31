package com.fleettracking.app.data;

/** Simple async result callback delivered on the main thread. */
public interface RepoCallback<T> {
    void onResult(T data);
    void onError(String message);
}
