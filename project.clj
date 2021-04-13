(defproject wordzert "0.1.0-SNAPSHOT"
  :description "A cooperative word game"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0" :url "https://www.eclipse.org/legal/epl-2.0/"}
  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [ring/ring-core "1.9.2"]
                 [ring/ring-anti-forgery "1.3.0"]
                 [http-kit "2.5.3"]
                 [compojure "1.6.2"]
                 [hiccup "1.0.5"]
                 [com.taoensso/sente "1.16.2"]
                 [org.slf4j/slf4j-simple "2.0.0-alpha1"]
                 [reagent "1.0.0"]]

  :source-paths ["src"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "wordzert.client.test-runner"]}

  :main ^:skip-aot wordzert.server.core

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[com.bhauman/figwheel-main "0.2.12"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]

                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["target"]}})
