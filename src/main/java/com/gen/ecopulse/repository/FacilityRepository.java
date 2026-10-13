package com.gen.ecopulse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gen.ecopulse.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
// JpaRepository provides CRUD operations and more for the Facility entity, with Long as the type of the primary key (id).
//JpaRepository provides ready-made methods, including save().
}