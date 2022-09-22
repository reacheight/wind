import { useRouter } from "next/router";
import { getUserContext } from "../../components/UserContextWrapper";
import { useEffect, useState } from "react";
import Routes from "../../api/routs";
import Match from "../../models/Match";
import styles from "../../styles/Match.module.css"
import Matchup from "../../components/Matchup";
import { AnalysisResult } from "../../models/AnalysisResult";
import AnalysisTable from "../../components/AnalysisTable";

const Match = () => {
  let userContext = getUserContext()
  if (userContext.loading || userContext.user === null)
    return null

  const router = useRouter()
  const { id: matchId } = router.query

  const [match, setMatch] = useState<Match>(null)
  const [analysis, setAnalysis] = useState<AnalysisResult>(null)

  useEffect(() => {
    fetch(Routes.Matches.get(matchId))
      .then(response => response.json())
      .then(json => setMatch(json))
  }, [])

  useEffect(() => {
    fetch(Routes.Analysis.get(matchId))
      .then(response => response.json())
      .then(json => setAnalysis(json))
      .catch(() => null)
  }, [])

  if (match == null)
    return null

  return (
    <div>
      <Matchup match={match}/>
      {analysis && <AnalysisTable analysis={analysis.analysis}/>}
    </div>
  )
}

export default Match