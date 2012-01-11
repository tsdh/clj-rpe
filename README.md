# clj-rpe

Clj-rpe is a Clojure library that implements *Regular Path Expressions* on
Clojure data structures and arbitrary Java objects.  Basically, it enables you
to retrieve the set of objects given a start object and a *regular path
description*.

A regular path description is a declarative means to specify allowed paths in
some object structure in terms of keys in a map or fields/methods in classes.
Those can be composed using regular operators such as sequence, option,
alternative, and iterations.

## Usage

Add a `(:use clj-rpe.core)` to your namespace declaration, and you are ready to
go.

Clj-rpe works for Clojure data structures (maps, records) and arbitrary Java
objects.  We'll start with the former.  For demonstration purposes, we'll
define some map `m`.  `m` could also be a record, because that's essentially a
map as well.

  (def m {:a 1
          "b" {1 "One", 2 "Two", 3 "Three"}
          :c {:foo {:x :foox, :y :fooy}
              :bar {:x :barx, :y :bary}
              :baz {:x :bazx, :y :bazy, :z :bazz}}
          :x {:y {:x {:y {:x {:y "Got me!"}}}}}})

The main function of clj-rpe is `rpe-reachables`.  It gets a start object (or a
collection of start objects) and a regular path description and returns the
ordered set of reachable objects.  In case of a map (or record), the most
simple path description is just a key that's looked up in the map.

What objects can be reached from `m` by traversing a path consisting only of
the key `:a`?

  (rpe-reachables m :a)
  ;=> #{1}
  (rpe-reachables m "b")
  ;=> #{{1 "One", 2 "Two", 3 "Three"}}

What happens when we use a key that's not in the map?

  (rpe-reachables m 'unkn0wn)
  ;=> #{}

We cannot reach anything inside `m` with that key, so the returned set is
empty.

### Regular Path Descriptions

Building upon the simple path descriptions (aka, keys in a map), we can define
regular path descriptions using the operators discussed in this section.

**Sequence.** The function `rpe-seq` is the path sequence returning the objects
reachable by traversing one path description after the other.  It is a
function, but usually you invoke it thru `rpe-reachables`.

  (rpe-reachables m [rpe-seq :c :bar :y])
  ;=> #{:bary}

**Option.** The function `rpe-opt` is the path option.  For example, let's get
all objects reachable by the path sequence in the last example except for `:y`
being traversed optionally now.

  (rpe-reachables m [rpe-seq :c :bar [rpe-opt :y]])
  ;=> #{{:y :bary, :x :barx}
        :bary}

**Alternative.** The function `rpe-alt` is the path alternative.  For example,
let's get all values of the `:y` key in `:foo`, `:bar`, and `:baz` submaps.

  (rpe-reachables m [rpe-seq :c [rpe-alt :foo :bar :baz] :y])
  ;=> #{:fooy :bary :bazy}

**Iteration.** The function `rpe-+` is the one-or-many path iteration, the
function `rpe-*` is the zero-or-many path iteration.

What can we reach by iterating an alternating path of `:x` and `:y` keys?

  (rpe-reachables m [rpe-+ [rpe-seq :x :y]])
  ;=> #{{:x {:y {:x {:y "Got me!"}}}}
        {:x {:y "Got me!"}}
        "Got me!"}

**Exponent.** The function `rpe-exp` is the path exponent.  It either iterates
the given path description a fixed number of times, or at least as often as the
given lower bound but at most as the given upper bound.

  (rpe-reachables m [rpe-exp 3 [rpe-seq :x :y]])
  ;=> #{"Got me!"}
  (rpe-reachables m [rpe-exp 2 19 [rpe-seq :x :y]])
  ;=> #{{:x {:y "Got me!"}} "Got me!"}

The iteration stops as soon as the last iteration doesn't find anything new.

**Restriction.** The function `rpe-restr` is the path restriction.  It's just
`filter` with swapped arguments, and it ensures that an ordered set is
returned.

  (rpe-reachables m [rpe-seq [rpe-+ [rpe-seq :x :y]]
                             [rpe-restr string?]])
  ;=> #{"Got me!"}


## License

Copyright (C) 2012 Tassilo Horn <tsdh80@googlemail.com>

Distributed under the Eclipse Public License, the same as Clojure.

<!-- Local Variables:        -->
<!-- mode: markdown          -->
<!-- indent-tabs-mode: nil   -->
<!-- End:                    -->
