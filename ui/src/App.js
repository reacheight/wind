import './App.css';
import React from 'react';

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      matchId: '',
      analysis: {},
    };

    this.handleChange = this.handleChange.bind(this)
    this.handleSubmit = this.handleSubmit.bind(this)
  }

  handleChange(event) {
    this.setState({ matchId: event.target.value })
  }

  handleSubmit(event) {
    fetch(`http://localhost:8080/analysis/${this.state.matchId}`)
      .then(response => response.json())
      .then(json => {
        this.setState({ analysis: json })
      })
    event.preventDefault()
  }

  render() {
    const analysis = this.state.analysis
    const analysisLoaded = analysis && Object.keys(analysis).length !== 0
    let courierInfo
    if (analysisLoaded) {
      courierInfo = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((id) =>
        <li key={id.toString}>
          {analysis.heroes[id]} courier is {analysis.couriers[id] ? "out of" : "in"} fountain
        </li>
      )
    }

    return (
      <div className="App">
        <form onSubmit={this.handleSubmit}>
          <input placeholder="Enter match id" type="text" value={this.state.matchId} onChange={this.handleChange} />
          <input type="submit" value="Analyze" />
        </form>
        {analysisLoaded &&
          <ul>{courierInfo}</ul>
        }
      </div>
    );
  }
}

export default App;
