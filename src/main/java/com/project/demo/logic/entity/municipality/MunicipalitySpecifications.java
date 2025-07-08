package com.project.demo.logic.entity.municipality;

import org.springframework.data.jpa.domain.Specification;

public class MunicipalitySpecifications {

    public static Specification<Municipality> hasNameContaining(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Municipality> hasCantonId(Long cantonId) {
        return (root, query, cb) ->
                (cantonId == null)
                        ? null
                        : cb.equal(root.get("canton").get("id"), cantonId);
    }

    public static Specification<Municipality> hasStatus(MunicipalityStatusEnum status) {
        return (root, query, cb) ->
                (status == null)
                        ? null
                        : cb.equal(root.get("status"), status);
    }
}
