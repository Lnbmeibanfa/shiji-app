package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.DishAliasEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishAliasRepository extends JpaRepository<DishAliasEntity, Long> {

    List<DishAliasEntity> findAllByOrderByIdAsc();
}
