package org.nextoracle.mapper;

import org.mapstruct.*;
import org.nextoracle.dto.AuthUserDto;
import org.nextoracle.entity.AuthUser;

/**
 * Mapper for the entity {@link AuthUser} and its DTO {@link AuthUserDto}.
 */
@Mapper(componentModel = "spring")
public interface AuthUserMapper extends EntityMapper<AuthUserDto, AuthUser> {

    @Override
    @Mapping(target = "auCreatedAt", ignore = true)
    @Mapping(target = "auLastLogin", ignore = true)
    AuthUser toEntity(AuthUserDto dto);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "auCreatedAt", ignore = true)
    @Mapping(target = "auLastLogin", ignore = true)
    void partialUpdate(@MappingTarget AuthUser source, AuthUser target);
}
