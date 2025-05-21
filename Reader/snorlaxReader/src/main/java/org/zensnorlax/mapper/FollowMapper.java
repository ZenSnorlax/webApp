package org.zensnorlax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.zensnorlax.model.pojo.BookKey;
import org.zensnorlax.model.pojo.Follow;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    @Insert("INSERT INTO follow (follower_id, followee_id) VALUES (#{followerId}, #{followeeId})")
    int insert(BookKey bookKey);
}
