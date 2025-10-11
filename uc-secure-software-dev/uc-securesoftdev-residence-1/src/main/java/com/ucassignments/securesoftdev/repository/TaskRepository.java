package com.ucassignments.securesoftdev.repository;


import com.ucassignments.securesoftdev.model.Task;
import com.ucassignments.securesoftdev.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {


    List<Task> findByUser(User user);
}