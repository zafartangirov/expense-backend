package com.expense.tracker.repository;

import com.expense.tracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate start, LocalDate end
    );

    List<Expense> findByUserIdAndCategoryIdOrderByDateDesc(
            Long userId, Long categoryId
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId " +
            "AND e.date BETWEEN :start AND :end")
    Double sumByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end " +
            "GROUP BY e.category.name")
    List<Object[]> sumByCategoryForUser(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}