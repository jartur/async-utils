# Async utils

This is a collection of potentially useful classes for working with 
async code.

## AsyncLoadingCache

This class is an async alternative to Guava cache, or rather it can be 
one some day in the future.

## SynchronizedAsyncSequencer

Use this to sequentialize the execution of your async code in regard 
to some abstract key. 

## Ideas

* Polling future
  * Waits for another future for at most specified time without blocking