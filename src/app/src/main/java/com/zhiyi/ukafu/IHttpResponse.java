package com.zhiyi.ukafu;

import java.io.IOException;

/**
 * 网络请求返回接口
 * Created by Administrator on 2018/10/9.
 */

public interface IHttpResponse {
    void OnHttpData(String data);
    void OnHttpDataError(IOException e);
}
