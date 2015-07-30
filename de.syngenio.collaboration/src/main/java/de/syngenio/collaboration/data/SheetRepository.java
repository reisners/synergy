package de.syngenio.collaboration.data;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SheetRepository extends GraphRepository<Sheet> {
    Iterable<Sheet> findByName(String name);
}