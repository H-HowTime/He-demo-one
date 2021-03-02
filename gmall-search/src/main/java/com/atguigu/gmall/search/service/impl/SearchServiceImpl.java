package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchRequestParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hehao
 * @create 2021-01-29 18:14
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchRequestParamVo searchRequestParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.sourceBuilder(searchRequestParamVo));
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析searchResponse
            SearchResponseVo searchResponseVo = this.parseSearchResponse(searchResponse);
            //通过搜索参数设置分页数据
            searchResponseVo.setPageNum(searchRequestParamVo.getPageNum());
            searchResponseVo.setPageSize(searchRequestParamVo.getPageSize());
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //解析hits 获取到总记录数和当前页数据
        SearchHits hits = searchResponse.getHits();
        //总记录数
        searchResponseVo.setTotal(hits.getTotalHits());
        //商品列表数据
        SearchHit[] hitsHits = hits.getHits();
        //将hitsHits反序列化为goods对象
        List<Goods> goodsList = Stream.of(hitsHits).map(attrs -> {

            String sourceAsString = attrs.getSourceAsString();
            //将attrs json字符串反序列化为goods对象
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //获取高亮字段，并替换
            Map<String, HighlightField> highlightFieldMap = attrs.getHighlightFields();
            HighlightField highlightField = highlightFieldMap.get("title");
            String highStr = highlightField.getFragments()[0].string();
            goods.setTitle(highStr);
            return goods;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);
        //解析aggregations 获取到品牌列表和分类列表和规格参数列表
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        if (!CollectionUtils.isEmpty(aggregationMap)) {
            //品牌列表 -- brandIdAgg -- 装换为可解析的long类型的词条聚合
            ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
            List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();
            if (!CollectionUtils.isEmpty(brandIdAggBuckets)) {
                //将brandIdAggBuckets集合装换为brand集合
                List<BrandEntity> brandEntityList = brandIdAggBuckets.stream().map(brandIdAggBucket -> {
                    BrandEntity brandEntity = new BrandEntity();
                    //获取品牌id
                    brandEntity.setId(brandIdAggBucket.getKeyAsNumber().longValue());
                    //获取子聚合  获取brandName和logo
                    Map<String, Aggregation> subAggMap = brandIdAggBucket.getAggregations().asMap();
                    if (!CollectionUtils.isEmpty(subAggMap)) {
                        //获取品牌name 子聚合brandNameAgg
                        ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggMap.get("brandNameAgg");
                        brandEntity.setName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                        //获取品牌logo 子聚合brandLogoAgg
                        ParsedStringTerms brandLogoAgg = (ParsedStringTerms) subAggMap.get("brandLogoAgg");
                        brandEntity.setLogo(brandLogoAgg.getBuckets().get(0).getKeyAsString());
                    }
                    return brandEntity;
                }).collect(Collectors.toList());
                searchResponseVo.setBrands(brandEntityList);
            }
            //分类列表 -- categoryIdAgg
            ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
            List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
            if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
                //将categoryIdAggBuckets集合装换为category集合
                List<CategoryEntity> categoryEntityList = categoryIdAggBuckets.stream().map(categoryIdAggBucket -> {
                    CategoryEntity categoryEntity = new CategoryEntity();
                    //获取分类id
                    categoryEntity.setId(categoryIdAggBucket.getKeyAsNumber().longValue());
                    //获取分类name 子聚合categoryNameAgg
                    Map<String, Aggregation> subAggMap = categoryIdAggBucket.getAggregations().asMap();
                    if (!CollectionUtils.isEmpty(subAggMap)) {
                        ParsedStringTerms categoryNameAgg = (ParsedStringTerms) subAggMap.get("categoryNameAgg");
                        categoryEntity.setName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                    }
                    return categoryEntity;
                }).collect(Collectors.toList());
                searchResponseVo.setCategories(categoryEntityList);
            }
            //规格参数列表 -- searchAttrAgg --nested嵌套聚合
            ParsedNested searchAttrAgg = (ParsedNested) aggregationMap.get("searchAttrAgg");
            Map<String, Aggregation> searchAttrAggMap = searchAttrAgg.getAggregations().asMap();
            if (!CollectionUtils.isEmpty(searchAttrAggMap)) {
                //获取子聚合 attrIdAgg
                ParsedLongTerms attrIdAgg = (ParsedLongTerms) searchAttrAggMap.get("attrIdAgg");
                List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
                    //将attrIdAggBuckets集合装换为SearchResponseAttrVo集合
                    List<SearchResponseAttrVo> responseAttrVoList = attrIdAggBuckets.stream().map(attrIdAggBucket -> {
                        SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                        //获取attrId
                        searchResponseAttrVo.setAttrId(attrIdAggBucket.getKeyAsNumber().longValue());
                        //获取子聚合
                        Map<String, Aggregation> subAgg = attrIdAggBucket.getAggregations().asMap();
                        //获取attrName attrNameAgg
                        ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAgg.get("attrNameAgg");
                        searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                        //获取attrValue attrValueAgg
                        ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAgg.get("attrValueAgg");
                        List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();
                        if (!CollectionUtils.isEmpty(valueAggBuckets)) {
                            searchResponseAttrVo.setAttrValues(valueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                        }
                        return searchResponseAttrVo;
                    }).collect(Collectors.toList());
                    searchResponseVo.setSearchAttrs(responseAttrVoList);
                }
            }
        }
        return searchResponseVo;
    }

    /**
     * 构建dsl语句
     *
     * @param searchRequestParamVo 请求参数
     * @return
     */
    private SearchSourceBuilder sourceBuilder(SearchRequestParamVo searchRequestParamVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1、关键字匹配
        String keyword = searchRequestParamVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //TODO 可以打广告
            return sourceBuilder;
        }
        boolQuery.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //2、构建过滤条件
        //2.1 构建品牌过滤
        List<Long> brandId = searchRequestParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        //2.2 构建分类过滤
        List<Long> categoryId = searchRequestParamVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQuery.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //2.3 构建价格区间过滤
        Double startPrice = searchRequestParamVo.getStartPrice();
        Double endPrice = searchRequestParamVo.getEndPrice();
        if (startPrice != null || endPrice != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (startPrice != null) {
                rangeQuery.gte(startPrice);
            }
            if (endPrice != null) {
                rangeQuery.lte(endPrice);
            }
            boolQuery.filter(rangeQuery);
        }
        //2.4 构建库存过滤
        Boolean store = searchRequestParamVo.getStore();
        if (store) {
            boolQuery.filter(QueryBuilders.termQuery("store", store));
        }
        //2.5 构建规格参数的嵌套过滤 ["4:6G-8G","5:128G-256G"]
        List<String> props = searchRequestParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2) {
                    BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
                    boolQuery1.must(QueryBuilders.termQuery("searchAttrs.attrId", attrs[0]));
                    boolQuery1.must(QueryBuilders.termsQuery("searchAttrs.attrValue", StringUtils.split(attrs[1], "-")));
                    //每一个prop对应一个嵌套过滤：1-对应嵌套锅炉中的path，2-嵌套过滤中的query，3-得分模式
                    boolQuery.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery1, ScoreMode.None));
                }
            });
        }
        sourceBuilder.query(boolQuery);
        //3、构建排序
        Integer sort = searchRequestParamVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1:
                    sourceBuilder.sort("price", SortOrder.ASC);
                    break;
                case 2:
                    sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 3:
                    sourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                case 4:
                    sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
            }
        }
        //4、构建分页
        Integer pageNum = searchRequestParamVo.getPageNum();
        Integer pageSize = searchRequestParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        //5、构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color:red;'>");
        highlightBuilder.postTags("</font>");
        sourceBuilder.highlighter(highlightBuilder);
        //6、构建聚合
        //6.1 构建品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("brandLogoAgg").field("logo")));
        //6.2 构建分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //6.3 构建规格参数嵌套聚合
        sourceBuilder.aggregation(
                AggregationBuilders.nested("searchAttrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //7、结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","defaultImage","price","title","sbuTitle"},null);
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
