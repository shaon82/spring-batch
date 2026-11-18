package com.leads.spring_batch.batch.launcher;

/** Starts a batch job. Callers (scheduler, future REST endpoint, ...) depend on this abstraction. */
public interface BatchJobTrigger {

  void trigger();
}
