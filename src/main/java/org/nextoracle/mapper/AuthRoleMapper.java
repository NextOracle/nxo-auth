package org.nextoracle.mapper;

import org.mapstruct.Mapper;
import org.nextoracle.dto.AuthRoleDto;
import org.nextoracle.entity.AuthRole;


/**
 * Mapper for the entity {@link AuthRole} and its DTO {@link AuthRoleDto}.
 */
@Mapper(componentModel = "spring")
public interface AuthRoleMapper extends EntityMapper<AuthRoleDto, AuthRole> {
}
