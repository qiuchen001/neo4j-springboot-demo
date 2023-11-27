package org.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.dto.tag.TagPage;
import org.example.dto.tag.TagPageTag;
import org.example.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Component
@RestController
@RequestMapping(value = "/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<JSONObject> addTag(@RequestBody JSONObject request) {
        String name = request.getString("name");
        String desc = request.getString("desc");
        String parentId = request.getString("parentId");
        String tagId = tagService.addTag(name, desc, parentId);

        JSONObject response = new JSONObject();
        response.put("code", 0);
        response.put("msg", "success");
        JSONObject data = new JSONObject();
        data.put("id", tagId);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{tagId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<JSONObject> updateTag(@PathVariable String tagId, @RequestBody JSONObject request) {
        String name = request.getString("name");
        String desc = request.getString("desc");
        String parentId = request.getString("parentId");
        tagService.updateTag(tagId, name, desc, parentId);

        JSONObject response = new JSONObject();
        response.put("code", 0);
        response.put("msg", "success");
        JSONObject data = new JSONObject();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value =  "/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<JSONObject> deleteTag(@PathVariable String tagId) {
        tagService.deleteTag(tagId);

        JSONObject response = new JSONObject();
        response.put("code", 0);
        response.put("msg", "success");
        JSONObject data = new JSONObject();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }


    @GetMapping(value =  "", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<JSONObject> tagPageList(@RequestParam(required = false) Long id,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String desc,
                                               @RequestParam(required = false) String parentName,
                                               @RequestParam(required = false) String createTimeRange,
                                               @RequestParam(required = false) String updateTimeRange,
                                               @RequestParam(required = false, defaultValue = "1") int page,
                                               @RequestParam(required = false, defaultValue = "10") int pagesize) {

        if (page < 1) {
            page = 1;
        }
        if (pagesize < 1) {
            pagesize = 10;
        }

        TagPage tagPage =  tagService.tagPageList(id, name, desc, parentName, createTimeRange, updateTimeRange, page, pagesize);
        JSONArray array = JSON.parseArray(JSON.toJSONString(tagPage.getTags()));

        JSONObject response = new JSONObject();
        response.put("code", 0);
        response.put("msg", "success");
        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("total", tagPage.getTotal());
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value =  "/all-tags", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<JSONObject> tagHierarchy() {
        List<TagPageTag> tagList = tagService.getTagHierarchy();
        JSONArray array = JSON.parseArray(JSON.toJSONString(tagList));

        JSONObject response = new JSONObject();
        response.put("code", 0);
        response.put("msg", "success");
        JSONObject data = new JSONObject();
        data.put("list", array);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

}



