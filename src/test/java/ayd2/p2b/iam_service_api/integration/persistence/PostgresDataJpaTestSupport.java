package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
abstract class PostgresDataJpaTestSupport {
}
