package com.storefit.support_service.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mensajes")
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Mensaje {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long senderUserId;

  @Column
  private Integer targetRoleId;

  @Column
  private Long targetUserId;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  private Long createdAt;

  @Column(nullable = false)
  private Boolean read = false;

  @Column(nullable = false)
  private Boolean isResponse = false;

  @Column
  private Long repliedToId;

  @Column
  private Long threadId;

  @Column
  private Long respondedAt;
}
