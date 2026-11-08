# Feenyx.ai Assessment Preparation Guide

## 1. Assessment Overview & Logistics
* **Content:** 6 Multiple-Choice Questions (MCQs), 1 Basic Coding Challenge, and 1 Front-End Challenge.
* **Supported Languages/Frameworks:** Java, JavaScript, Python, Angular, Bootstrap, HTML/CSS/JS, ReactJS, VueJS.
* **Proctoring / Verification:** Google Chrome or Firefox required. Government-issued ID upload and facial verification required before starting.
* **Pacing Rule:** Once submitted, MCQs cannot be revisited. Manage your time carefully.

---

## Part 1: Java & Backend Core (10 Practice MCQs & 9 Coding Challenges)

### Multiple-Choice Questions

1. **What is the primary difference between `String`, `StringBuilder`, and `StringBuffer` in Java?**
   * A) `String` is mutable; `StringBuilder` and `StringBuffer` are immutable.
   * B) `StringBuilder` is thread-safe and synchronized; `StringBuffer` is not.
   * C) `String` is immutable; `StringBuilder` is mutable and unsynchronized; `StringBuffer` is mutable and synchronized.
   * D) There is no performance difference between them.
   * **Answer:** C

2. **Which of the following functional interfaces represents an operation that accepts a single input argument and returns no result?**
   * A) `Predicate<T>`
   * B) `Function<T, R>`
   * C) `Consumer<T>`
   * D) `Supplier<T>`
   * **Answer:** C

3. **In Spring Boot, which annotation is used to inject a value from `application.properties` into a class field?**
   * A) `@Autowired`
   * B) `@Value`
   * C) `@Inject`
   * D) `@PropertySource`
   * **Answer:** B

4. **What will be the output of the following Java Stream operation given a list of integers `[1, 2, 3, 4]`?**
   ```java
   int sum = list.stream().filter(n -> n % 2 == 0).mapToInt(n -> n * 2).sum();
   ```
   * A) 6
   * B) 10
   * C) 12
   * D) 20
   * **Answer:** C (Even numbers are 2 and 4. Doubled they become 4 and 8. Sum = 12).

5. **Which exception type must be caught or declared in a method signature in Java?**
   * A) `RuntimeException`
   * B) `NullPointerException`
   * C) `IOException`
   * D) `ArithmeticException`
   * **Answer:** C

6. **What is the time complexity of searching for an element in a balanced `TreeSet` containing $N$ elements?**
   * A) $O(1)$
   * B) $O(\log N)$
   * C) $O(N)$
   * D) $O(N \log N)$
   * **Answer:** B

7. **How does the `equals()` and `hashCode()` contract work in Java collections?**
   * A) If two objects are equal according to `equals()`, they must have the same hash code.
   * B) If two objects have the same hash code, they must be equal according to `equals()`.
   * C) `hashCode()` is only used for sorting.
   * D) There is no relationship required between them.
   * **Answer:** A

8. **Which garbage collection collector is the default in modern Java (Java 11+) for server-class machines?**
   * A) Serial GC
   * B) CMS (Concurrent Mark Sweep)
   * C) G1 GC (Garbage-First)
   * D) ZGC
   * **Answer:** C

9. **In a Spring REST controller, what is the default scope of a `@RestController` bean?**
   * A) Prototype
   * B) Request
   * C) Session
   * D) Singleton
   * **Answer:** D

10. **What is the purpose of the `volatile` keyword in Java?**
    * A) To make a variable constant and immutable.
    * B) To ensure that changes made to a variable are immediately visible across multiple threads.
    * C) To automatically synchronize blocks of code.
    * D) To prevent garbage collection of an object.
    * **Answer:** B

### Java Practice Coding Challenges

1. **Two Sum**
   * **Problem:** Write a Java method `public int[] findTwoSum(int[] nums, int target)` that returns the indices of the two numbers such that they add up to a specific target. You may assume that each input would have exactly one solution, and you may not use the same element twice.
   * **Example:** `nums = [2, 7, 11, 15]`, `target = 9` $\rightarrow$ Returns `[0, 1]`.

2. **Valid Parentheses**
   * **Problem:** Write a Java method `public boolean isValid(String s)` that determines if an input string containing `(`, `)`, `{`, `}`, `[` and `]` is valid. Open brackets must be closed by the same type of brackets in the correct order.
   * **Example:** `s = "()[]{}"` $\rightarrow$ `true`, `s = "(]"` $\rightarrow$ `false`.

3. **First Non-Repeating Character**
   * **Problem:** Write a Java method `public int firstUniqChar(String s)` that returns the index of the first non-repeating character in a string. If it does not exist, return `-1`.
   * **Example:** `s = "leetcode"` $\rightarrow$ `0` (character `'l'`), `s = "loveleetcode"` $\rightarrow$ `2` (character `'v'`).

4. **Merge Overlapping Intervals**
   * **Problem:** Given an array of intervals where `intervals[i] = [start_i, end_i]`, write a Java method `public int[][] merge(int[][] intervals)` that merges all overlapping intervals and returns an array of the non-overlapping intervals.
   * **Example:** `[[1,3],[2,6],[8,10],[15,18]]` $\rightarrow$ `[[1,6],[8,10],[15,18]]`.

5. **Least Recently Used (LRU) Cache**
   * **Problem:** Design and implement a data structure for a Least Recently Used (LRU) cache in Java extending `LinkedHashMap` or using custom nodes with capacity `capacity`. Support `get(int key)` and `put(int key, int value)` in $O(1)$ time complexity.

6. **Stream Grouping and Aggregation**
   * **Problem:** Given a list of `Transaction` objects (fields: `String category`, `double amount`), write a Java method using Streams (`Collectors.groupingBy`) that returns a `Map<String, Double>` representing the total sum of amounts grouped by category.

7. **Reverse a Singly Linked List**
   * **Problem:** Given the head of a singly linked list, write a Java method `public ListNode reverseList(ListNode head)` that reverses the list and returns the reversed list's head.
   * **Example:** `1 -> 2 -> 3 -> NULL` $\rightarrow$ `3 -> 2 -> 1 -> NULL`.

8. **Find the Duplicate Number**
   * **Problem:** Given an array of integers `nums` containing $n + 1$ integers where each integer is in the range $[1, n]$ inclusive, write a Java method `public int findDuplicate(int[] nums)` to find the duplicate number without modifying the array and using constant extra space.

9. **Longest Substring Without Repeating Characters**
   * **Problem:** Given a string `s`, write a Java method `public int lengthOfLongestSubstring(String s)` to find the length of the longest substring without repeating characters.
   * **Example:** `s = "abcabcbb"` $\rightarrow$ `3` (The answer is `"abc"`).

---

## Part 2: Angular & Frontend Core (10 Practice MCQs & 9 Challenges)

### Multiple-Choice Questions

1. **What is the primary purpose of Angular Modules (`@NgModule`)?**
   * A) To compile CSS styles into JavaScript.
   * B) To consolidate components, directives, pipes, and providers into cohesive blocks of functionality.
   * C) To replace standard ECMAScript modules.
   * D) To manage HTTP routing directly on the server.
   * **Answer:** B

2. **Which lifecycle hook in Angular is called only once after component initialization and after data-bound properties are initialized?**
   * A) `ngOnInit`
   * B) `ngOnChanges`
   * C) `ngAfterViewInit`
   * D) `ngOnDestroy`
   * **Answer:** A

3. **What is the difference between `FormGroup` and `FormControl` in Angular Reactive Forms?**
   * A) `FormControl` tracks the value and validation state of an individual form control; `FormGroup` tracks the same for a group of form controls.
   * B) `FormGroup` is used exclusively for template-driven forms.
   * C) `FormControl` cannot have validators attached to it.
   * D) There is no structural difference.
   * **Answer:** A

4. **How do you pass data from a parent component to a child component in Angular?**
   * A) Using `@Output()` and `EventEmitter`
   * B) Using `@Input()` decorator
   * C) Using Services without dependency injection
   * D) Using local storage variables
   * **Answer:** B

5. **What is an Observable in RxJS?**
   * A) A function that returns a single value synchronously.
   * B) A lazy Push collection of multiple values over time.
   * C) An immutable array representation in TypeScript.
   * D) A built-in DOM manipulation utility.
   * **Answer:** B

6. **Which RxJS operator transforms items emitted by an Observable by applying a function to each item?**
   * A) `filter`
   * B) `switchMap`
   * C) `map`
   * D) `tap`
   * **Answer:** C

7. **What is the purpose of Angular Directives with an asterisk (`*ngIf`, `*ngFor`)?**
   * A) Structural directives that alter the layout structure by adding or removing DOM elements.
   * B) Attribute directives that change the appearance of an element.
   * C) Pipe transformations for string interpolation.
   * D) Routing guards for security.
   * **Answer:** A

8. **What does Ahead-of-Time (AOT) compilation do in Angular?**
   * A) Compiles your TypeScript and HTML code during the browser runtime.
   * B) Compiles your TypeScript and HTML code during the build phase before the browser downloads and runs it.
   * C) Minifies CSS stylesheets only.
   * D) Generates unit test suites automatically.
   * **Answer:** B

9. **How do you inject a service into an Angular component correctly in modern Angular (v14+)?**
   * A) Using the `constructor(private myService: MyService)` pattern or `inject(MyService)` function.
   * B) Using the `@Injectable({ global: true })` flag.
   * C) Importing the service class directly into the HTML template.
   * D) Declaring the service inside `tsconfig.json`.
   * **Answer:** A

10. **What is the function of the `async` pipe in Angular templates?**
    * A) It makes standard HTTP calls asynchronous in the background without subscriptions.
    * B) It subscribes to an Observable or Promise and returns the latest emitted value, automatically managing unsubscription when the component is destroyed.
    * C) It forces synchronous rendering of heavy components.
    * D) It delays component rendering by 500ms.
    * **Answer:** B

### Angular Practice Front-End Challenges

1. **Reactive Search Filter**
   * **Problem Statement:** Build an Angular component containing a search input field (`FormControl`) and a list display. Filter an array of mock user objects (name, email, role) in real-time using RxJS operators like `debounceTime`, `distinctUntilChanged`, and `switchMap` or array `.filter()`.

2. **Reactive Debounced Search Control**
   * **Problem Statement:** Build a standalone Angular component containing a search input using Reactive Forms. Use RxJS operators (`debounceTime(300)`, `distinctUntilChanged()`, `switchMap()`) to trigger an asynchronous search service call only when user input stabilizes.

3. **Signal-Based Shopping Cart with Computed Total**
   * **Problem Statement:** Create an Angular component using modern Signals (`signal()`, `computed()`) that manages a list of cart items. Expose a computed signal that automatically recalculates the total order price whenever item quantities or unit prices change.

4. **Custom Structural Directive (`*appHasRole`)**
   * **Problem Statement:** Implement a custom structural directive in Angular that takes a required permission string input (e.g., `*appHasRole="'ADMIN'"`) and conditionally adds or removes the host DOM element from the view based on a mock authentication service state.

5. **Observable-Driven Data Table with Sorting**
   * **Problem Statement:** Construct an Angular component that consumes an RxJS Observable data stream of user records. Implement local sorting functionality (by name or email) and pipe the stream through sorting logic to display paginated rows in the template using the `async` pipe.

6. **Custom Form Control with ControlValueAccessor**
   * **Problem Statement:** Build a custom rating component (e.g., star rating) that implements `ControlValueAccessor` so it can be seamlessly integrated into Angular Reactive Forms using `formControlName`.

7. **Dynamic Tab Navigation Component**
   * **Problem Statement:** Build a reusable Angular component using `@ContentChildren` or component binding to manage and render dynamic tabs, where users can switch views and pass context inputs down to active tabs.

8. **Auth Interceptor with Token Refresh Flow**
   * **Problem Statement:** Implement an Angular HTTP Interceptor (`HttpInterceptorFn` or class-based) that intercepts outgoing requests to attach a Bearer JWT token, catches `401 Unauthorized` responses, and triggers a token refresh retry queue.

9. **Lazy-Loaded Feature Module with Route Guards**
   * **Problem Statement:** Create an Angular routing setup containing a lazy-loaded admin module protected by a functional route guard (`CanActivateFn`) that checks authentication states before allowing navigation.