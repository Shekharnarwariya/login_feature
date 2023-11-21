package com.hti.smpp.common.route.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;

import jakarta.transaction.Transactional;

@Repository
public interface RouteEntryRepository extends JpaRepository<RouteEntry, Integer> {

	public List<RouteEntry> findAll(Specification<RouteEntry> spec);

	public List<RouteEntry> findByUserId(int userId);

	public void deleteByUserId(int userId);

	public void deleteByUserIdAndNetworkIdIn(int userId, List<Integer> networkIds);

	public List<RouteEntry> findByUserIdInAndSmscIdInAndGroupIdInAndNetworkIdInAndSmscTypeInAndCostBetween(
			int[] userIds, int[] smscIds, int[] groupIds, int[] networkIds, String[] smscTypes, double minCost,
			double maxCost);

	@Transactional
	@Modifying
	@Query(value = "update RouteOpt A, RouteOptSch B set A.isReplaceContent = B.isReplaceContent, "
			+ "A.contentReplace = B.contentReplace, A.backupSmscId = B.backupSmscId, "
			+ "A.numSmscId = B.numSmscId, A.regSmscId = B.regSmscId, A.regSenderId = B.regSenderId, "
			+ "A.forceSidNum = B.forceSidNum, A.forceSidAlpha = B.forceSidAlpha, A.setExpiry = B.setExpiry, "
			+ "A.smsLength = B.smsLength, A.codeLength = B.codeLength, A.refund = B.refund, "
			+ "A.msgAppender = B.msgAppender, A.sourceAppender = B.sourceAppender, A.editBy = B.scheduleBy, "
			+ "A.editOn = B.scheduleOn, A.senderReplFrom = B.senderReplFrom, A.senderReplTo = B.senderReplTo, "
			+ "A.regGroupId = B.regGroupId "
			+ "where A.routeId = B.routeId and B.scheduleOn = :scheduledOn", nativeQuery = true)
	public int updateOptRouteSchAndDelete(String scheduledOn);

	@Query(value = "SELECT new com.hti.smpp.common.route.dto.RouteEntryExt(A.userId, A.networkId, B.routeId, B.isReplaceContent, B.contentReplace, B.backupSmscId, B.numSmscId, B.regSmscId, B.regGroupId, B.regSenderId, B.forceSIDNum, B.forceSIDAlpha, B.setExpiry, B.smsLength, B.codeLength, B.refund, B.editOn, B.msgAppender, B.sourceAppender, B.editBy, B.senderReplFrom, B.senderReplTo, C.country, C.operator, D.name as backupSmsc, F.name as numSmsc, G.name as regSmsc, E.systemId, H.name as regGroupName) "
			+ "FROM RouteMaster A, RouteOpt B, Network C, SmscMaster D, SmscMaster F, SmscMaster G, UserMaster E, SmscGroup H "
			+ "WHERE B.routeId IN :criterionEntries AND A.id = B.routeId AND B.backupSmscId = D.id AND B.numSmscId = F.id AND B.regSmscId = G.id AND A.userId = E.id AND A.networkId = C.id AND B.regGroupId = H.id", nativeQuery = true)
	public List<RouteEntryExt> getRoutingList(@Param("criterionEntries") List<String> criterionEntries);

}
