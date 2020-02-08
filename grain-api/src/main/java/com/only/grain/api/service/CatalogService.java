package com.only.grain.api.service;

import com.only.grain.api.bean.PmsBaseCatalog1;
import com.only.grain.api.bean.PmsBaseCatalog2;
import com.only.grain.api.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {
    public List<PmsBaseCatalog1> getCatalog1();
    public List<PmsBaseCatalog2> getCatalog2(String Catalog1Id);
    public List<PmsBaseCatalog3> getCatalog3(String Catalog2Id);
}
