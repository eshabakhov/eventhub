package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.exception.TagNotFoundException;
import org.kmb.eventhub.exception.UnexpectedException;
import org.kmb.eventhub.repository.TagRepository;
import org.kmb.eventhub.tables.daos.TagDao;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        return tagDao.fetchOptionalById(id).orElseThrow(() -> new TagNotFoundException(id));
    }

    public Tag update(Tag tag) {
        tagDao.update(tag);
        return tag;
    }

    public void delete(Long id) {
        if (tagDao.fetchOptionalById(id).isEmpty()) {
            throw new TagNotFoundException(id);
        }
        if (!Objects.isNull(tagRepository.fetchUnused(id))) {
            throw new UnexpectedException("Существуют мероприятия с указанным тегом");
        }
        tagDao.deleteById(id);
    }

    public List<Tag> checkAllTags(List<Tag> tagList) {
        List<Tag> result = new ArrayList<>();
        tagList.forEach(tag-> {
            Tag existingTag = tagRepository.findTagByName(tag.getName());
            if (existingTag == null) {
                result.add(tagRepository.createTag(tag.getName()));
            }
            else {
                result.add(existingTag);
            }
        });
        return result;
    }


    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return tagRepository.getUsedTagIdsForEvent(eventId);
    }


    public void assignTagsToEvent(Long eventId, List<Tag> tags) {
         tagRepository.assignNewEventTag(eventId,tags);
    }
}
