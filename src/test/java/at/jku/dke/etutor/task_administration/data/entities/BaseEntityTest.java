package at.jku.dke.etutor.task_administration.data.entities;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.AbstractLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void testEqualsSameReturnsTrue() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = entity1;

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsSameIdReturnsTrue() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = new TestEntity(1);

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsIdNullReturnsFalse() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = new TestEntity(null);

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsSameIdHibernateProxyReturnsTrue() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = new HibernateTestProxy(1);

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsThisHibernateProxySameIdReturnsTrue() {
        // Arrange
        var entity1 = new HibernateTestProxy(1);
        var entity2 = new TestEntity(1);

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEqualsNotSameIdReturnsFalse() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = new TestEntity(2);

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsOtherNullReturnsFalse() {
        // Arrange
        var entity1 = new TestEntity(1);

        // Act
        var result = entity1.equals(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEqualsOtherClassReturnsFalse() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = "Test";

        // Act
        var result = entity1.equals(entity2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHashCodeSameClassEquals() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = new TestEntity(2);

        // Act
        var result1 = entity1.hashCode();
        var result2 = entity2.hashCode();

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void testHashCodeOtherClassNotEquals() {
        // Arrange
        var entity1 = new TestEntity(1);
        var entity2 = "test";

        // Act
        var result1 = entity1.hashCode();
        var result2 = entity2.hashCode();

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testHashCodeHibernateProxyEquals() {
        // Arrange
        var entity1 = new TestEntity(1);
        TestEntity entity2 = new HibernateTestProxy(2);

        // Act
        var result1 = entity1.hashCode();
        var result2 = entity2.hashCode();

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void testToString() {
        // Arrange
        var entity = new TestEntity(1);

        // Act
        var result = entity.toString();

        // Assert
        assertEquals("TestEntity[id=1]", result);
    }

    private static class TestEntity extends BaseEntity<Integer> {
        private Integer id;

        public TestEntity(Integer id) {
            this.id = id;
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }
    }

    private static class HibernateTestProxy extends TestEntity implements HibernateProxy {

        public HibernateTestProxy(int id) {
            super(id);
        }

        @Override
        public Object writeReplace() {
            return null;
        }

        @Override
        public LazyInitializer getHibernateLazyInitializer() {
            return new LazyInitializer() {
                @Override
                public void initialize() throws HibernateException {

                }

                @Override
                public Object getIdentifier() {
                    return null;
                }

                @Override
                public void setIdentifier(Object id) {

                }

                @Override
                public String getEntityName() {
                    return null;
                }

                @Override
                public Class<?> getPersistentClass() {
                    return TestEntity.class;
                }

                @Override
                public boolean isUninitialized() {
                    return false;
                }

                @Override
                public Object getImplementation() {
                    return null;
                }

                @Override
                public Object getImplementation(SharedSessionContractImplementor session) throws HibernateException {
                    return null;
                }

                @Override
                public void setImplementation(Object target) {

                }

                @Override
                public Class<?> getImplementationClass() {
                    return null;
                }

                @Override
                public String getImplementationEntityName() {
                    return null;
                }

                @Override
                public boolean isReadOnlySettingAvailable() {
                    return false;
                }

                @Override
                public boolean isReadOnly() {
                    return false;
                }

                @Override
                public void setReadOnly(boolean readOnly) {

                }

                @Override
                public SharedSessionContractImplementor getSession() {
                    return null;
                }

                @Override
                public void setSession(SharedSessionContractImplementor session) throws HibernateException {

                }

                @Override
                public void unsetSession() {

                }

                @Override
                public void setUnwrap(boolean unwrap) {

                }

                @Override
                public boolean isUnwrap() {
                    return false;
                }
            };
        }
    }
}
