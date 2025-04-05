package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.repository.TagRepository;
import org.kmb.eventhub.tables.daos.TagDao;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    private final TagDao tagDao;
    public ResponseList<Tag> getList(Integer page, Integer pageSize) {
        ResponseList<Tag> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<Tag> list =  tagRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(tagRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public Tag create(Tag tag) {
        tagDao.insert(tag);
        return tag;
    }

    public Tag get(Long id) {
        return tagDao.findById(id);
    }

    public Tag update(Tag tag) {
        tagDao.update(tag);
        return tag;
    }

    public boolean delete(Long id) {
        tagDao.deleteById(id);
        return true;
    }
}
