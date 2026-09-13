package com.lisovskyi.jpa.autoconfigure.generator;

import org.hibernate.generator.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class EntitySequenceGeneratorTest {

    private final EntitySequenceGenerator generator = new EntitySequenceGenerator();

    @ParameterizedTest
    @CsvSource({
        "UserEntity, user_entity_seq_gen",
        "ProductOrder, product_order_seq_gen",
        "Invoice, invoice_seq_gen",
        "Tag, tag_seq_gen"
    })
    void convertsCamelCaseClassNameToSnakeCaseSequenceName(String simpleClassName, String expectedSequenceName)
            throws Exception {
        Method toSequenceName = EntitySequenceGenerator.class.getDeclaredMethod("toSequenceName", String.class);
        toSequenceName.setAccessible(true);

        String sequenceName = (String) toSequenceName.invoke(generator, simpleClassName);

        assertThat(sequenceName).isEqualTo(expectedSequenceName);
    }

    @Test
    void extractsSimpleNameFromFullyQualifiedClassName() throws Exception {
        Method extractSimpleName = EntitySequenceGenerator.class.getDeclaredMethod("extractSimpleName", String.class);
        extractSimpleName.setAccessible(true);

        String simpleName = (String) extractSimpleName.invoke(generator, "com.example.UserEntity");

        assertThat(simpleName).isEqualTo("UserEntity");
    }

    @Test
    void extractSimpleNameReturnsInputWhenThereIsNoPackage() throws Exception {
        Method extractSimpleName = EntitySequenceGenerator.class.getDeclaredMethod("extractSimpleName", String.class);
        extractSimpleName.setAccessible(true);

        String simpleName = (String) extractSimpleName.invoke(generator, "UserEntity");

        assertThat(simpleName).isEqualTo("UserEntity");
    }

    @Test
    void generateReturnsExistingNonZeroIdWithoutCallingTheSequence() {
        // currentValue is non-null and non-zero, so generate() must return it directly
        // without touching the session — passing null for session/owner is therefore safe.
        Object generated = generator.generate(null, new Object(), 5L, EventType.INSERT);

        assertThat(generated).isEqualTo(5L);
    }
}
