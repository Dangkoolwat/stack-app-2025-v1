package com.daangcool.stack.web.rest.common;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.service.common.CommonCodeService;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.DetailDto;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.GroupDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
/**
 * REST controller for managing CommonCodeGroup and CommonCodeDetail (Admin Only).
 */
@RestController
@RequestMapping("/api/common")
public class CommonCodeResource {

    private static final Logger LOG = LoggerFactory.getLogger(CommonCodeResource.class);

    private static final String ENTITY_GROUP = "commonCodeGroup";
    private static final String ENTITY_DETAIL = "commonCodeDetail";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CommonCodeService commonCodeService;

    public CommonCodeResource(CommonCodeService commonCodeService) {
        this.commonCodeService = commonCodeService;
    }

    // --- CommonCodeGroup Endpoints (/api/common/groups) ---

    /**
     * {@code POST  /groups} : Creates a new common code group.
     * 관리자 권한 필요
     *
     * @param group the CommonCodeGroup to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)}.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CommonCodeGroup> createGroup(@Valid @RequestBody CommonCodeGroup group) throws URISyntaxException {
        LOG.debug("REST request to save CommonCodeGroup : {}", group);

        CommonCodeGroup result = commonCodeService.createGroup(group);

        return ResponseEntity.created(new URI("/api/common/groups/" + result.getGroupCode()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_GROUP, result.getGroupCode()))
            .body(result);
    }

    /**
     * {@code PUT  /groups} : Updates an existing common code group.
     * 관리자 권한 필요
     *
     * @param group the CommonCodeGroup to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PutMapping("/groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CommonCodeGroup> updateGroup(@Valid @RequestBody CommonCodeGroup group) {
        LOG.debug("REST request to update CommonCodeGroup : {}", group);

        return commonCodeService.updateGroup(group)
            .map(result -> ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_GROUP, result.getGroupCode()))
                .body(result))
            .orElseThrow(() -> new BadRequestAlertException("Group not found", ENTITY_GROUP, "notfound"));
    }

    /**
     * {@code GET  /groups} : Get all non-deleted common code groups.
     * 관리자 권한 필요
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of groups in body.
     */
    @GetMapping("/groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<GroupDto> getAllGroups() {
        LOG.debug("REST request to get all non-deleted CommonCodeGroups");
        return commonCodeService.findAllGroups();
    }

    /**
     * {@code GET  /groups/:groupCode} : Get the common code group by code.
     * 관리자 권한 필요
     *
     * @param groupCode the code of the group to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @GetMapping("/groups/{groupCode}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GroupDto> getGroup(@PathVariable String groupCode) {
        LOG.debug("REST request to get CommonCodeGroup : {}", groupCode);
        return ResponseUtil.wrapOrNotFound(commonCodeService.findGroup(groupCode));
    }

    /**
     * {@code DELETE  /groups/:groupCode} : Soft delete the common code group by code.
     * 관리자 권한 필요
     *
     * @param groupCode the code of the group to soft delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/groups/{groupCode}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> softDeleteGroup(@PathVariable String groupCode) {
        LOG.debug("REST request to soft delete CommonCodeGroup : {}", groupCode);
        commonCodeService.softDeleteGroup(groupCode);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_GROUP, groupCode)).build();
    }


    // --- CommonCodeDetail Endpoints (/api/common/details) ---

    /**
     * {@code POST  /details} : Creates a new common code detail.
     * 관리자 권한 필요
     *
     * @param detail the CommonCodeDetail to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)}.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/details")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CommonCodeDetail> createDetail(@Valid @RequestBody CommonCodeDetail detail) throws URISyntaxException {
        LOG.debug("REST request to save CommonCodeDetail : {}", detail);

        CommonCodeDetail result = commonCodeService.createDetail(detail);

        return ResponseEntity.created(new URI("/api/common/details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_DETAIL, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /details} : Updates an existing common code detail.
     * 관리자 권한 필요
     *
     * @param detail the CommonCodeDetail to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PutMapping("/details")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CommonCodeDetail> updateDetail(@Valid @RequestBody CommonCodeDetail detail) {
        LOG.debug("REST request to update CommonCodeDetail : {}", detail);

        return commonCodeService.updateDetail(detail)
            .map(result -> ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_DETAIL, result.getId().toString()))
                .body(result))
            .orElseThrow(() -> new BadRequestAlertException("Detail not found", ENTITY_DETAIL, "notfound"));
    }

    /**
     * {@code GET  /details/group/:groupCode} : Get all non-deleted common code details for a given group.
     * 관리자 권한 필요
     *
     * @param groupCode the group code.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of details in body.
     */
    @GetMapping("/details/group/{groupCode}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<DetailDto> getAllDetailsByGroup(@PathVariable String groupCode) {
        LOG.debug("REST request to get all non-deleted CommonCodeDetails for group: {}", groupCode);
        return commonCodeService.findAllDetailsByGroup(groupCode);
    }

    /**
     * {@code GET  /details/:id} : Get the common code detail by ID.
     * 관리자 권한 필요
     *
     * @param id the ID of the detail to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @GetMapping("/details/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<DetailDto> getDetail(@PathVariable Long id) {
        LOG.debug("REST request to get CommonCodeDetail : {}", id);
        return ResponseUtil.wrapOrNotFound(commonCodeService.findDetail(id));
    }

    /**
     * {@code DELETE  /details/:id} : Soft delete the common code detail by ID.
     * 관리자 권한 필요
     *
     * @param id the ID of the detail to soft delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/details/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> softDeleteDetail(@PathVariable Long id) {
        LOG.debug("REST request to soft delete CommonCodeDetail : {}", id);
        commonCodeService.softDeleteDetail(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_DETAIL, id.toString())).build();
    }
}
