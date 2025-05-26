# dashboards/vecindash.py
import os, requests, pandas as pd, streamlit as st
from dateutil import parser as dt

TOKEN   = os.getenv("CLICKUP_TOKEN")     # viene de secreto GH
LIST_ID = os.getenv("CLICKUP_LIST_ID")   # idem
HEADERS = {"Authorization": TOKEN}

@st.cache_data(ttl=60)
def fetch_tasks():
    url = f"https://api.clickup.com/api/v2/list/{LIST_ID}/task"
    tasks = requests.get(url, headers=HEADERS,
                         params={"archived":"false"}).json()["tasks"]
    return pd.DataFrame([{
        "id"    : t["id"],
        "name"  : t["name"],
        "status": t["status"]["status"],
        "start" : dt.parse(t["start_date"]) if t["start_date"] else None,
        "due"   : dt.parse(t["due_date"])   if t["due_date"]   else None,
        "url"   : f'https://app.clickup.com/t/{t["id"]}'
    } for t in tasks])

def burndown(df):
    df["date"] = pd.to_datetime(df["due"].fillna(df["start"]\
                                                 .fillna(pd.Timestamp.today())))
    rng = pd.date_range(df["date"].min(), df["date"].max()).to_series()
    counts = {s: rng.copy().map(lambda x: 0)
              for s in ["todo","in progress","done"]}
    for _, r in df.iterrows(): counts[r["status"].lower()]\
        .loc[r["date"]:] += 1
    return pd.DataFrame(counts)

st.title("Vecin-Dash – Sprint tracker")
if st.button("🔄 Refresh"): st.cache_data.clear()
df = fetch_tasks()
tab1, tab2, tab3 = st.tabs(["Kanban","Burndown","Timeline"])

with tab1:
    for col in ["to do","in progress","review","done"]:
        st.subheader(col.title())
        for _, r in df[df["status"].str.lower()==col].iterrows():
            st.markdown(f"- [{r['name']}]({r['url']})")

with tab2: st.line_chart(burndown(df))
with tab3:  st.dataframe(df[["name","start","due","status"]])
