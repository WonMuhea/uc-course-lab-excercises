/*
package com.ucassignments.securesoftdev.repository;
import com.ucassignments.securesoftdev.model.Widget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WidgetRepository extends JpaRepository<Widget, Long> {

    Optional<Widget> findByName(String name);

    @Query("select w from Widget w " +
            "where (:q is null or lower(w.name) like lower(concat('%', :q, '%')) " +
            "   or lower(w.description) like lower(concat('%', :q, '%')))")
    Page<Widget> search(@Param("q") String q, Pageable pageable);
}

*/
