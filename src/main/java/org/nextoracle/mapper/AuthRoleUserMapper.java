package org.nextoracle.mapper;

import org.mapstruct.Mapper;
import org.nextoracle.dto.AuthRoleUserDto;
import org.nextoracle.entity.AuthRoleUser;

/**
 * Mapper for the entity {@link AuthRoleUser} and its DTO {@link AuthRoleUserDto}.
 */
@Mapper(componentModel = "spring", uses = {AuthUserMapper.class, AuthRoleMapper.class})
public interface AuthRoleUserMapper extends EntityMapper<AuthRoleUserDto, AuthRoleUser> {
}
