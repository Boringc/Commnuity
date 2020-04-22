package com.boring.community.dao;

import com.boring.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset , int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment (Comment comment);
}
