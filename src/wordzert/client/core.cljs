(ns ^:figwheel-hooks wordzert.client.core
  (:require
   [goog.dom :as gdom]
   [taoensso.sente :as sente]
   [reagent.core :as reagent]
   [reagent.dom :as reagent-dom]
   ))

(println "This text is printed from src/hello_world/core.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk" ?csrf-token {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state))  ; Watchable, read-only atom

;; We can watch this atom for changes if we like
(add-watch chsk-state :chsk-state
           (fn [_ _ old new]
             (when (not= old new)
               (println "chsk-state change: %s" new))))

(defn atom-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn start-game [name]
  (js/alert (str "Starting game with player name: " name))
  (chsk-send! [:wordzert/start-game {:name name}]))

(defn join-game [name code]
  (js/alert (str "Joining game with player name: " name ", game code: " code))
  (chsk-send! [:wordzert/join-game {:name name :code code}]))

(defn sign-in-widget []
  (let [name (reagent/atom "")
        code (reagent/atom "")]
    (fn []
      [:div
       [:p "Enter Your Name:" [atom-input name]]
       [:p "Enter Game Code:" [atom-input code]
        [:button {:type "button" :on-click #(join-game @name @code)} "Join Game"]
        ]
       [:p "Or " [:button {:type "button" :on-click #(start-game @name)} "Start New Game"]]])))

(reagent-dom/render [sign-in-widget] (.getElementById js/document "app"))


;; TODO - Get initial state from server
;; then render based on that state.
;; That way refreshing the browser while in
;; a game will put you back into the game.
