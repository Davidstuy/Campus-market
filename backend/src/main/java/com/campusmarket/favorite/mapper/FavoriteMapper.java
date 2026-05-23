package com.campusmarket.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
