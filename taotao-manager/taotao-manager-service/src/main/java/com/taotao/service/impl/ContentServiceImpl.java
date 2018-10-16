package com.taotao.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EUDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.HttpClientUtil;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;
import com.taotao.service.ContentService;

/**
 * 广告内容管理业务实现层
 * 
 * @author kangyong
 * @date 2018年7月7日
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Value("${REST_BASE_URL}")
	private String REST_BASE_URL;
	@Value("${REST_CONTENT_SYNC_URL}")
	private String REST_CONTENT_SYNC_URL;

	@Override
	public EUDataGridResult getContentList(long categoryId, int page, int rows) {
		// 封裝查询条件
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		// 分页条件
		PageHelper.startPage(page, rows);
		// 执行查询
		List<TbContent> list = contentMapper.selectByExample(example);
		PageInfo<TbContent> pageInfo = new PageInfo<>(list);

		// 封装结果
		EUDataGridResult result = new EUDataGridResult();
		result.setRows(list);
		result.setTotal(pageInfo.getTotal());
		return result;
	}

	@Override
	public TaotaoResult insertContent(TbContent content) {
		// 补全pojo
		Date date = new Date();
		content.setCreated(date);
		content.setUpdated(date);
		// 执行插入
		contentMapper.insert(content);

		// 添加缓存同步逻辑
		try {
			HttpClientUtil.doGet(REST_BASE_URL + REST_CONTENT_SYNC_URL + content.getCategoryId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return TaotaoResult.ok();
	}

}
