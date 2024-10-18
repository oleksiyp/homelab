package app;

import codegen.jooq.page_with_db.backend.tables.Person;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Require;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemsService {
    private final DSLContext create;

    public List<String> items() {
        return create.selectFrom(Person.PERSON)
                .fetch(Person.PERSON.FIRST_NAME);
    }
}
