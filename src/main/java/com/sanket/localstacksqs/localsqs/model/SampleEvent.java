package com.sanket.localstacksqs.localsqs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SampleEvent {

  private String eventId;
  private String version;
  private String type;
  private ZonedDateTime eventTime;
  private EventData data;
}
