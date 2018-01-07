package com.heady.headyecom;

/**
 * Created by adhiraj on 8/9/15.
 */
public interface IAsyncCallback {
    public void onSuccessResponse(String successResponse);

    public void onErrorResponse(int errorCode, String errorResponse);
}
