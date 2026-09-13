package com.lisovskyi.jpa.autoconfigure.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    private static class TestEntity extends BaseEntity {
        TestEntity() {}

        TestEntity(Long id) {
            setId(id);
        }
    }

    private static class OtherEntity extends BaseEntity {
        OtherEntity(Long id) {
            setId(id);
        }
    }

    @Test
    void isEqualToItself() {
        TestEntity entity = new TestEntity(1L);

        assertThat(entity).isEqualTo(entity);
    }

    @Test
    void twoEntitiesWithTheSameIdAreEqual() {
        TestEntity first = new TestEntity(1L);
        TestEntity second = new TestEntity(1L);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void twoEntitiesWithDifferentIdsAreNotEqual() {
        TestEntity first = new TestEntity(1L);
        TestEntity second = new TestEntity(2L);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void twoTransientEntitiesAreNeverEqual() {
        TestEntity first = new TestEntity();
        TestEntity second = new TestEntity();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void isNotEqualToNull() {
        assertThat(new TestEntity(1L)).isNotEqualTo(null);
    }

    @Test
    void isNotEqualToAnUnrelatedType() {
        assertThat(new TestEntity(1L)).isNotEqualTo("not-an-entity");
    }

    @Test
    void entitiesOfDifferentSubclassesWithTheSameIdAreStillEqual() {
        // equals() only checks "instanceof BaseEntity" plus a matching non-null id — it does
        // not compare getClass(), so this holds even across unrelated BaseEntity subclasses.
        TestEntity entity = new TestEntity(1L);
        OtherEntity other = new OtherEntity(1L);

        assertThat(entity).isEqualTo(other);
    }

    @Test
    void hashCodeIsStableAcrossTheTransientToPersistentLifecycle() {
        TestEntity entity = new TestEntity();
        int transientHash = entity.hashCode();

        entity.setId(1L);

        assertThat(entity.hashCode()).isEqualTo(transientHash);
    }

    @Test
    void hashCodeIsBasedOnTheConcreteClass() {
        TestEntity entity = new TestEntity(1L);

        assertThat(entity.hashCode()).isEqualTo(TestEntity.class.hashCode());
    }
}
