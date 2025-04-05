package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.tables.daos.UserDao;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserDao userDao;

    public ResponseList<User> getList(Integer page, Integer pageSize) {
        ResponseList<User> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<User> list =  userRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public User create(User user) {
        user.setIsActive(true);
        userDao.insert(user);
        return user;
    }

    public User get(Long id) {
        return userDao.findById(id);
    }

    public User update(User user) {
        userDao.update(user);
        return user;
    }

    public boolean delete(Long id) {
        userDao.deleteById(id);
        return true;
    }
}
