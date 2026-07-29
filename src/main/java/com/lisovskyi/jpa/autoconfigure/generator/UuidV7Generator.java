package com.lisovskyi.jpa.autoconfigure.generator;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class UuidV7Generator implements IdentifierGenerator {

    /**
     * Generates a UUID v7 (time-ordered) identifier.
     * <p>
     * If the entity already has an assigned ID (e.g. during merge or
     * programmatic insert with a pre-set key), that value is returned
     * as-is. This allows callers to control the ID when needed.
     */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner) {
        try {
            var persister = session.getEntityPersister(null, owner);
            Object existingId = persister.getIdentifier(owner, session);
            if (existingId != null) {
                return existingId;
            }
        } catch (Exception ignored) {
            // If the persister lookup fails for any reason, fall through and generate a new ID.
        }
        return UuidCreator.getTimeOrderedEpoch();
    }
}
