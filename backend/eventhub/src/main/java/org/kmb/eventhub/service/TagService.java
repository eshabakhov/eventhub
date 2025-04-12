package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.TagDTO;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.exception.TagNotFoundException;
import org.kmb.eventhub.mapper.TagMapper;
import org.kmb.eventhub.repository.TagRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.TagDao;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final EventDao eventDao;

    private final TagMapper tagMapper;

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

    @Transactional
    public Tag create(TagDTO tagDTO) {
        Tag tag = tagMapper.toEntity(tagDTO);
        tagDao.insert(tag);
        return tag;
    }

    public Tag get(Long id) {
        return tagDao.fetchOptionalById(id).orElseThrow(() -> new TagNotFoundException(id));
    }

    @Transactional
    public Long delete(Long id, EventDTO eventDTO) {
        if (tagDao.fetchOptionalById(id).isEmpty()) {
            throw new TagNotFoundException(id);
        }
        eventDao.fetchOptionalById(eventDTO.getId()).orElseThrow(() -> new EventNotFoundException(eventDTO.getId()));

        tagRepository.delete(id, eventDTO);
        if (tagRepository.tagIsUsed(id)) {
            tagDao.deleteById(id);
        }
        return id;
    }

    public List<Tag> checkAllTags(List<Tag> tagList) {
        List<Tag> result = new ArrayList<>();
        tagList.forEach(tag-> {
            Tag existingTag = tagRepository.findTagByName(tag.getName());
            if (Objects.isNull(existingTag)) {
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
