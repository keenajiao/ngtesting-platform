package com.ngtesting.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ngtesting.platform.config.Constant;
import com.ngtesting.platform.dao.TestVerDao;
import com.ngtesting.platform.model.TstUser;
import com.ngtesting.platform.model.TstVer;
import com.ngtesting.platform.service.TestVerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestVerServiceImpl extends BaseServiceImpl implements TestVerService {
    @Autowired
    TestVerDao verDao;

    @Override
    public List<TstVer> list(Integer projectId, String keywords, Boolean disabled) {
        List<TstVer> ls = verDao.query(projectId, keywords, disabled);
        return ls;
    }

    @Override
    public TstVer getById(Integer id, Integer projectId) {
        TstVer po = verDao.get(id, projectId);
        return po;
    }

    @Override
    public TstVer save(JSONObject json, TstUser user) {
        Integer id = json.getInteger("id");

        TstVer vo = JSON.parseObject(JSON.toJSONString(json), TstVer.class);
        vo.setProjectId(user.getDefaultPrjId());

        Constant.MsgType action;
        if (id != null) {
            action = Constant.MsgType.update;

            verDao.update(vo);
        } else {
            action = Constant.MsgType.create;

            Integer maxOrder = verDao.getMaxOrdrNumb(vo.getProjectId());
            if (maxOrder == null) {
                maxOrder = 0;
            }
            vo.setOrdr(maxOrder + 10);

            verDao.add(vo);
        }

        return vo;
    }

    @Override
    public void delete(Integer id, Integer projectId) {
        verDao.delete(id, projectId);
    }

    @Override
    @Transactional
    public boolean changeOrder(Integer id, String act, Integer projectId) {
        TstVer curr = verDao.get(id, projectId);
        if (curr == null) {
            return false;
        }

        TstVer neighbor = null;
        if ("up".equals(act)) {
            neighbor = verDao.getPrev(curr.getOrdr(), projectId);
        } else if ("down".equals(act)) {
            neighbor = verDao.getNext(curr.getOrdr(), projectId);
        }
        if (neighbor == null) {
            return false;
        }

        Integer currOrder = curr.getOrdr();
        Integer neighborOrder = neighbor.getOrdr();
        verDao.setOrder(id, neighborOrder, projectId);
        verDao.setOrder(neighbor.getId(), currOrder, projectId);

        return true;
    }

}

