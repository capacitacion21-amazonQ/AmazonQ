package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.entity.TicketStatus;
import com.example.ticketero.model.entity.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoReferenciaAndNationalId(String codigoReferencia, String nationalId);

    List<Ticket> findBySucursalIdAndStatusOrderByCreatedAtAsc(Long sucursalId, TicketStatus status);

    List<Ticket> findBySucursalIdAndTipoServicioAndStatusOrderByCreatedAtAsc(
        Long sucursalId, TipoServicio tipoServicio, TicketStatus status);

    @Query("""
        SELECT COUNT(t) FROM Ticket t
        WHERE t.sucursalId = :sucursalId
        AND t.tipoServicio = :tipoServicio
        AND t.status = :status
        AND t.createdAt < :createdAt
        """)
    long countTicketsAnteriores(
        @Param("sucursalId") Long sucursalId,
        @Param("tipoServicio") TipoServicio tipoServicio,
        @Param("status") TicketStatus status,
        @Param("createdAt") LocalDateTime createdAt
    );

    List<Ticket> findByStatusAndEjecutivoIdIsNull(TicketStatus status);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.sucursalId = :sucursalId
        AND t.createdAt >= :fechaInicio
        AND t.createdAt <= :fechaFin
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findBySucursalAndFechaRange(
        @Param("sucursalId") Long sucursalId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}