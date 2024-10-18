package app;

import codegen.openapi.page_with_db.api.ItemsApi;
import codegen.openapi.page_with_db.api.GetItems200ResponseInner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/items/v1")
@RequiredArgsConstructor
public class ItemsController implements ItemsApi {
    private final ItemsService itemsService;

    @Override
    public ResponseEntity<List<GetItems200ResponseInner>> getItems() {
        List<GetItems200ResponseInner> items = new ArrayList<>();
        for (String value : itemsService.items()) {
            GetItems200ResponseInner item = new GetItems200ResponseInner();
            item.name(value);
            items.add(item);
        }
        return ResponseEntity.ok(items);
    }
}
