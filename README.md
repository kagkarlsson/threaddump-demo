# Thread-dump demo application

## Exercise

1. Start the `ThreaddumpDempApplication` using your IDE or mvn
    ```shell
    mvn spring-boot:run
    ```
2. Trigger the scenario you want to test. Example scenario `database_read` here:
   ```shell
   curl http://localhost:8080/fakework/database_read
   ```
3. Take the thread-dump, for example using `jcmd` (`| less` makes it easier to browse/search).
   ```shell
   jcmd no.bekk.threaddumpdemo.ThreaddumpDemoApplication Thread.print | less
   ```

4. Find and inspect the relevant thread to see what the stack looks like for that particular scenario.
    **Hint:** search for packages specific for this app, e.g. `no.bekk.threaddumpdemo`

## Alternative ways to get a thread-dump

**Hint:** Use a no-arg `jcmd` to list pids and classnames for running java-processes (might not work in some envs). 

```shell
jstack <pid>
jcmd <pid> Thread.print
jcmd <class-name> Thread.print

# If no JDK, SIGQUIT dumps threads to System.out
kill -QUIT 

# For a Kubernetes pod (Java pid is typically 1)
kubectl exec <pod-name> -- jstack 1
```

## Scenarios

```
curl http://localhost:8080/fakework/<SCENARIO>
```

* `database_read` - Slow database-query
* `tcp_connect` - Tcp-connect does not complete before timeout
* `http_client_get` - Slow third-party webservice
* `db_pool_get_connection` - Forced to wait for connections as pool all connections occupied
* `lock_contention` - Forced to wait for lock held by another thread
* `cpu_loop` - Slow local loop (i.e. pure cpu)

## Thread states

- `RUNNABLE` - thread is currently executing/can be executed in the jvm
- `BLOCKED` - thread is blocked indefinitely while waiting for a lock (typically synchronized)
- `TIMED_WAITING` - thread is waiting for a period of time (triggered by e.g Thread.sleep(..))
- `WAITING` - thread is waiting indefinitely for another thread to perform a certain action (i.e. Object.wait()->Object.notify, ...)

## Resources

* [https://www.baeldung.com/java-thread-dump](https://www.baeldung.com/java-thread-dump)
* [https://www.baeldung.com/java-analyze-thread-dumps](https://www.baeldung.com/java-analyze-thread-dumps)
