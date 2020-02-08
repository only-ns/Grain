package com.only.grain.api.service;

import com.only.grain.api.bean.PmsSearchParam;
import com.only.grain.api.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
