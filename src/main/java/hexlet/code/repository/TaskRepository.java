package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.labels WHERE t.id = :id")
    Optional<Task> findByIdFetchLabels(@Param("id") long id);

    @Query("""
            SELECT
                t
            FROM Task t
            LEFT JOIN FETCH t.labels l
            LEFT JOIN t.assignee a
            LEFT JOIN t.taskStatus s
            WHERE
                (t.name LIKE %:titleCont% OR :titleCont = "")
                AND (a.id = :assigneeId OR :assigneeId = 0)
                AND (s.slug = :status OR :status = "")
                AND (l.id = :labelId OR :labelId = 0)
            """)
    List<Task> findAllUsingFilters(
            @Param("titleCont") String titleCont,
            @Param("assigneeId") long assigneeId,
            @Param("status") String status,
            @Param("labelId") long labelId);
}
